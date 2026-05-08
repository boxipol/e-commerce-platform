package com.pd.ecommerce.service;

import com.pd.ecommerce.event.OrderEventProducer;
import com.pd.ecommerce.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;


@Slf4j
@Service
@RequiredArgsConstructor
public final class OrderServiceImpl implements OrderService {

//	private final PaymentServiceClient orderServiceClient;
	private final OrderRepository orderRepository;
//	private final OrderMapper orderMapper;
	private final OrderEventProducer eventProducer;

	@Override
	public Mono<String> getData() {
		return Mono.just("Order Service is up and running!");
	}

//	@Override
//	public OrderResponse getOrder(Long orderId) {
//		return orderRepository.findById(orderId)
//			.map(orderMapper::toResponse)
//			.orElseThrow(() -> new OrderNotFoundException(orderId));
//	}
//
//	@Override
//	public OrderResponse placeOrder(OrderRequest request) {
//		Order order = orderMapper.toOrder(request);
////		order.setStatus(OrderStatus.PENDING);
////		order.setCreatedAt(Instant.now());
//
//		Order saved = orderRepository.save(order);
//
//		// Publish event to Kafka
//		eventProducer.publish(new OrderPlacedEvent(saved.getId(), saved.getUserId()));
//
//		return orderMapper.toResponse(saved);
//	}
}