package com.pd.ecommerce.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pd.ecommerce.client.ProductServiceClient;
import com.pd.ecommerce.dto.CreateOrderRequest;
import com.pd.ecommerce.dto.OrderItemRequest;
import com.pd.ecommerce.dto.OrderItemResponse;
import com.pd.ecommerce.dto.OrderResponse;
import com.pd.ecommerce.dto.ProductSnapshot;
import com.pd.ecommerce.entity.Order;
import com.pd.ecommerce.entity.OrderItem;
import com.pd.ecommerce.entity.OrderStatus;
import com.pd.ecommerce.entity.OutboxEvent;
import com.pd.ecommerce.entity.OutboxEventStatus;
import com.pd.ecommerce.event.OrderCreatedEvent;
import com.pd.ecommerce.exception.OrderNotFoundException;
import com.pd.ecommerce.mapper.OrderMapper;
import com.pd.ecommerce.repository.OrderItemRepository;
import com.pd.ecommerce.repository.OrderRepository;
import com.pd.ecommerce.repository.OutboxEventRepository;
import io.r2dbc.postgresql.codec.Json;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

	private final ProductServiceClient productServiceClient;
	private final OrderRepository orderRepository;
	private final OrderItemRepository orderItemRepository;
	private final OutboxEventRepository outboxEventRepository;
	private final OrderMapper mapper;
	private final ObjectMapper objectMapper;


	public Mono<OrderResponse> getOrder(String publicOrderId) {
		return orderRepository.findByPublicOrderId(publicOrderId)
			.switchIfEmpty(Mono.error(new OrderNotFoundException(publicOrderId)))
			.flatMap(order ->
				orderItemRepository.findByOrderId(order.getId())
					.collectList()
					.map(items -> buildResponse(order, items))
			);
	}

	@Override
	public Flux<OrderResponse> getOrdersByUser(UUID userId) {
		return orderRepository.findByUserId(userId)
			.flatMap(order ->
				orderItemRepository.findByOrderId(order.getId())
					.collectList()
					.map(items -> buildResponse(order, items))
			);
	}

	@Transactional
	public Mono<OrderResponse> createOrder(UUID userId, String userMail, CreateOrderRequest request) {
		List<String> productSkus = request.items()
			.stream()
			.map(OrderItemRequest::sku)
			.toList();

		return productServiceClient.getProducts(productSkus)
			.map(products -> products.stream()
				.collect(Collectors
					.toMap(ProductSnapshot::sku, Function.identity())))
			.flatMap(productMap -> {
				List<OrderItem> items = buildOrderItems(request, productMap);
				BigDecimal totalAmount = calculateTotal(items);
				Instant createdAt = Instant.now();

				Order order = Order.builder()
					.userId(userId)
					.userMail(userMail)
					.publicOrderId(generatePublicOrderId())
					.status(OrderStatus.CREATED)
					.totalAmount(totalAmount)
					.createdAt(createdAt)
					.build();

				return orderRepository.save(order)
					.flatMap(savedOrder -> {
						items.forEach(item -> item.setOrderId(savedOrder.getId()));
						OrderCreatedEvent event = mapper.toOrderCreatedEvent(savedOrder, items);

						OutboxEvent outboxEvent = OutboxEvent.builder()
							.aggregateType("ORDER")
							.aggregateId(savedOrder.getId())
							.eventType("ORDER_CREATED")
							.payload(Json.of(toJson(event)))
							.status(OutboxEventStatus.PENDING)
							.createdAt(createdAt)
							.publishedAt(createdAt)
							.build();

						log.info("Saving outbox event for order: {}", savedOrder.getId());

						return orderItemRepository.saveAll(items)
							.then(
								outboxEventRepository.save(outboxEvent)
							)
							.thenReturn(
								mapper.toResponse(event.userMail(), savedOrder, items)
							);
					});
			});
	}

	@Override
	@Transactional
	public Mono<Void> markAsPaid(UUID orderId) {
		Instant updatedAt = Instant.now();

		return orderRepository.markAsPaid(orderId, updatedAt)
			.flatMap(rows -> outboxEventRepository.markProcessed(orderId, updatedAt))
			.then();
	}

	@Override
	@Transactional
	public Mono<Void> markAsFailed(UUID orderId) {
		Instant updatedAt = Instant.now();

		return orderRepository.markAsCanceled(orderId, updatedAt)
			.flatMap(rows -> outboxEventRepository.markAsFailed(orderId, updatedAt))
			.then();
	}

//	==================== PRIVATE ====================

	private OrderResponse buildResponse(Order order, List<OrderItem> items) {
		List<OrderItemResponse> itemsResponse = items.stream()
			.map(mapper::toItemResponse)
			.toList();

		return OrderResponse.builder()
			.userMail(order.getUserMail())
			.publicOrderId(order.getPublicOrderId())
			.status(order.getStatus())
			.totalAmount(order.getTotalAmount())
			.createdAt(order.getCreatedAt())
			.items(itemsResponse)
			.build();
	}

	private List<OrderItem> buildOrderItems(CreateOrderRequest request, Map<String, ProductSnapshot> productMap) {
		return request.items()
			.stream()
			.map(orderItemRequest -> {
				ProductSnapshot product = productMap.get(orderItemRequest.sku());

				if (product == null) {
					throw new RuntimeException("Product not found: " + orderItemRequest.sku());
				}
				return OrderItem.builder()
					.productId(productMap.get(orderItemRequest.sku()).productId())
					.sku(orderItemRequest.sku())
					.quantity(orderItemRequest.quantity())
					.unitPrice(product.price())
					.subtotal(
						product.price()
							.multiply(BigDecimal.valueOf(orderItemRequest.quantity()))
					)
					.build();
			})
			.toList();
	}

	private BigDecimal calculateTotal(List<OrderItem> items) {
		return items.stream()
			.map(item -> item.getSubtotal() != null ? item.getSubtotal() : BigDecimal.ZERO)
			.reduce(BigDecimal.ZERO, BigDecimal::add);
	}

	private String toJson(Object value) {
		try {
			return objectMapper.writeValueAsString(value);
		} catch (JsonProcessingException e) {
			throw new IllegalStateException("Failed to serialize outbox event", e);
		}
	}

	private static String generatePublicOrderId() {
		String shortUuid = UUID.randomUUID()
			.toString()
			.replace("-", "")
			.substring(0, 8)
			.toUpperCase();

		return "ORD-" + shortUuid;
	}
}