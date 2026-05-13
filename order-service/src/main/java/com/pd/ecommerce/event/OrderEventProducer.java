package com.pd.ecommerce.event;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderEventProducer {

	private final KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate;


	public void publish(OrderCreatedEvent event) {
		kafkaTemplate.send("order.created", event.orderId(), event);
	}
}