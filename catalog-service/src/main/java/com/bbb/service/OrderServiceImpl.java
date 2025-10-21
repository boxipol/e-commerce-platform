package com.bbb.service;

import com.bbb.dto.OrderRequest;
import com.bbb.dto.OrderResponse;
import com.bbb.entity.Order;
import com.bbb.event.OrderEventProducer;
import com.bbb.event.OrderPlacedEvent;
import com.bbb.exception.OrderNotFoundException;
import com.bbb.mapper.OrderMapper;
import com.bbb.repository.OrderRepository;
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