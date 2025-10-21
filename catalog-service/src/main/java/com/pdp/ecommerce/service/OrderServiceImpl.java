package com.pdp.ecommerce.service;

import com.pdp.ecommerce.dto.OrderRequest;
import com.pdp.ecommerce.dto.OrderResponse;
import com.pdp.ecommerce.entity.Order;
import com.pdp.ecommerce.event.OrderEventProducer;
import com.pdp.ecommerce.event.OrderPlacedEvent;
import com.pdp.ecommerce.exception.OrderNotFoundException;
import com.pdp.ecommerce.mapper.OrderMapper;
import com.pdp.ecommerce.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

	private final OrderRepository orderRepository;
	private final OrderMapper orderMapper;
	private final OrderEventProducer eventProducer;


	@Override
	public OrderResponse getOrder(Long orderId) {
		return orderRepository.findById(orderId)
			.map(orderMapper::toResponse)
			.orElseThrow(() -> new OrderNotFoundException(orderId));
	}

	@Override
	public OrderResponse placeOrder(OrderRequest request) {
		Order order = orderMapper.toOrder(request);
//		order.setStatus(OrderStatus.PENDING);
//		order.setCreatedAt(Instant.now());

		Order saved = orderRepository.save(order);

		// Publish event to Kafka
		eventProducer.publish(new OrderPlacedEvent(saved.getId(), saved.getUserId()));

		return orderMapper.toResponse(saved);
	}
}