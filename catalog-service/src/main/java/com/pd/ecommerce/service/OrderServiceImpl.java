package com.pd.ecommerce.service;

import com.pd.ecommerce.client.OrderServiceClient;
import com.pd.ecommerce.dto.OrderRequest;
import com.pd.ecommerce.dto.OrderResponse;
import com.pd.ecommerce.entity.Order;
import com.pd.ecommerce.event.OrderEventProducer;
import com.pd.ecommerce.event.OrderPlacedEvent;
import com.pd.ecommerce.exception.OrderNotFoundException;
import com.pd.ecommerce.mapper.OrderMapper;
import com.pd.ecommerce.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

	private final OrderServiceClient orderServiceClient;
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

	public void printOrder(Long id) {
		OrderResponse orderResponse = orderServiceClient.getOrderById(id);
		log.info("Fetched order: {}", orderResponse);
	}
}