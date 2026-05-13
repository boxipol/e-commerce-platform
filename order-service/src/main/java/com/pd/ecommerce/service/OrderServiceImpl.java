package com.pd.ecommerce.service;

import com.pd.ecommerce.client.ProductServiceClient;
import com.pd.ecommerce.dto.CreateOrderItemRequest;
import com.pd.ecommerce.dto.CreateOrderRequest;
import com.pd.ecommerce.dto.OrderItemResponse;
import com.pd.ecommerce.dto.OrderResponse;
import com.pd.ecommerce.dto.ProductSnapshot;
import com.pd.ecommerce.entity.Order;
import com.pd.ecommerce.entity.OrderItem;
import com.pd.ecommerce.mapper.OrderMapper;
import com.pd.ecommerce.repository.OrderItemRepository;
import com.pd.ecommerce.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public final class OrderServiceImpl implements OrderService {

	private final ProductServiceClient productServiceClient;
	private final OrderRepository orderRepository;
	private final OrderItemRepository orderItemRepository;
	private final OrderMapper mapper;
//	private final OrderEventProducer eventProducer;


	public Mono<OrderResponse> getOrder(UUID id) {
		Mono<Order> orderMono = orderRepository.findById(id);
		Mono<List<OrderItem>> itemsMono = orderItemRepository.findByOrderId(id)
			.collectList();

		return Mono.zip(orderMono, itemsMono).map(tuple -> {
			Order order = tuple.getT1();
			List<OrderItem> items = tuple.getT2();

			return buildResponse(order, items);
		});
	}

	public Mono<OrderResponse> createOrder(CreateOrderRequest request) {
		List<UUID> productIds = request.items()
			.stream()
			.map(CreateOrderItemRequest::productId)
			.toList();

		return productServiceClient.getProducts(productIds)
			.map(products -> products.stream()
				.collect(Collectors.toMap(ProductSnapshot::id, Function.identity())))
			.flatMap(productMap -> {
				List<OrderItem> items = request.items()
					.stream()
					.map(req -> {
						ProductSnapshot product = productMap.get(req.productId());

						if (product == null) {
							throw new RuntimeException("Product not found: " + req.productId());
						}

						return OrderItem.builder()
							.id(UUID.randomUUID())
							.productId(req.productId())
							.quantity(req.quantity())
							.unitPrice(product.price())
							.subtotal(product.price().multiply(BigDecimal.valueOf(req.quantity())))
							.build();
					}).toList();

					BigDecimal totalAmount = items.stream()
						.map(OrderItem::getSubtotal)
						.reduce(BigDecimal.ZERO, BigDecimal::add);

					Order order = Order.builder()
						.id(UUID.randomUUID())
						.userId(request.userId())
						.status("CREATED")
						.totalAmount(totalAmount)
						.createdAt(Instant.now())
						.build();

					return orderRepository.save(order)
						.flatMap(savedOrder -> {
							List<OrderItem> orderItems = items.stream().map(item -> {
								item.setOrderId(savedOrder.getId());
								return item;
							}).toList();

							return orderItemRepository.saveAll(orderItems)
								.collectList()
								.map(savedItems -> mapper.toResponse(savedOrder, savedItems));
						});
			});
	}

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
}