package com.pd.ecommerce.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pd.ecommerce.client.ProductServiceClient;
import com.pd.ecommerce.dto.CreateOrderItemRequest;
import com.pd.ecommerce.dto.CreateOrderRequest;
import com.pd.ecommerce.dto.OrderItemResponse;
import com.pd.ecommerce.dto.OrderResponse;
import com.pd.ecommerce.dto.ProductSnapshot;
import com.pd.ecommerce.entity.Order;
import com.pd.ecommerce.entity.OrderItem;
import com.pd.ecommerce.entity.OrderStatus;
import com.pd.ecommerce.entity.OutboxEvent;
import com.pd.ecommerce.entity.OutboxStatus;
import com.pd.ecommerce.event.OrderCreatedEvent;
import com.pd.ecommerce.mapper.OrderMapper;
import com.pd.ecommerce.repository.OrderItemRepository;
import com.pd.ecommerce.repository.OrderRepository;
import com.pd.ecommerce.repository.OutboxEventRepository;
import io.r2dbc.postgresql.codec.Json;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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


	public Mono<OrderResponse> getOrder(UUID id) {
		Mono<Order> orderMono = orderRepository.findById(id);
		Mono<List<OrderItem>> itemsMono = orderItemRepository.findByOrderId(id)
			.collectList();

		return Mono.zip(orderMono, itemsMono)
			.map(tuple -> {
				Order order = tuple.getT1();
				List<OrderItem> items = tuple.getT2();

			return buildResponse(order, items);
		});
	}

	@Transactional
	public Mono<OrderResponse> createOrder(CreateOrderRequest request) {
		List<UUID> productIds = request.items()
			.stream()
			.map(CreateOrderItemRequest::productId)
			.toList();

		return productServiceClient.getProducts(productIds)
			.map(products -> products.stream()
				.collect(Collectors.toMap(ProductSnapshot::id, Function.identity())))
			.flatMap(productMap -> {
				List<OrderItem> items = buildOrderItems(request, productMap);
				BigDecimal totalAmount = calculateTotal(items);
				Instant createdAt = Instant.now();

				Order order = Order.builder()
					.userId(request.userId())
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
							.status(OutboxStatus.PENDING)
							.createdAt(createdAt)
							.publishedAt(createdAt)
							.build();

						log.info("Saving outbox event for order {}", savedOrder.getId());

						return orderItemRepository.saveAll(items)
							.then(
								outboxEventRepository.save(outboxEvent)
									.doOnNext(saved ->
										log.info("OUTBOX SAVED: {}", saved.getId())
									)
							)
							.thenReturn(
								mapper.toResponse(savedOrder, items)
							)
							.doOnSuccess(v -> log.info("TX SUCCESS"))
							.doOnError(e -> log.error("TX FAILED", e))
							.doFinally(sig -> log.info("TX FINALLY: {}", sig));
					});
			});
	}

	@Override
	@Transactional
	public Mono<Void> markAsPaid(UUID orderId) {
		Instant updatedAt = Instant.now();

		return orderRepository.markAsPaid(orderId, updatedAt)
			.then(outboxEventRepository.markProcessed(orderId, updatedAt))
			.then();
	}

	@Override
	@Transactional
	public Mono<Void> markAsFailed(UUID orderId) {
		Instant updatedAt = Instant.now();

		return orderRepository.markAsCanceled(orderId, updatedAt)
			.then(outboxEventRepository.markAsFailed(orderId, updatedAt))
			.then();
	}

//	==================== PRIVATE ====================

	private OrderResponse buildResponse(Order order, List<OrderItem> items) {
		List<OrderItemResponse> itemResponses = items.stream()
			.map(mapper::toItemResponse)
			.toList();

		return OrderResponse.builder()
			.id(order.getId())
			.userId(order.getUserId())
			.status(order.getStatus())
			.totalAmount(order.getTotalAmount())
			.createdAt(order.getCreatedAt())
			.items(itemResponses)
			.build();
	}

	private List<OrderItem> buildOrderItems(CreateOrderRequest request, Map<UUID, ProductSnapshot> productMap) {
		return request.items()
			.stream()
			.map(orderItemRequest -> {
				ProductSnapshot product = productMap.get(orderItemRequest.productId());

				if (product == null) {
					throw new RuntimeException("Product not found: " + orderItemRequest.productId());
				}

				return OrderItem.builder()
					.productId(orderItemRequest.productId())
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
}