package org.bbb.service;

import lombok.RequiredArgsConstructor;
import org.bbb.dto.OrderRequest;
import org.bbb.dto.OrderResponse;
import org.bbb.entity.Order;
import org.bbb.entity.OrderStatus;
import org.bbb.event.OrderEventProducer;
import org.bbb.event.OrderPlacedEvent;
import org.bbb.exception.OrderNotFoundException;
import org.bbb.mapper.OrderMapper;
import org.bbb.repository.OrderRepository;
import org.springframework.stereotype.Service;
import java.time.Instant;

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
		Order order = orderMapper.toEntity(request);
		order.setStatus(OrderStatus.PENDING);
		order.setCreatedAt(Instant.now());

		Order saved = orderRepository.save(order);

		// Publish event to Kafka
		eventProducer.publish(new OrderPlacedEvent(saved.getId(), saved.getUserId()));

		return orderMapper.toResponse(saved);
	}
}