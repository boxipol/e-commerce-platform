package com.bbb.event;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderEventProducer {

	private final KafkaTemplate<String, OrderPlacedEvent> kafkaTemplate;


	public void publish(OrderPlacedEvent event) {
		kafkaTemplate.send("orders", event);
	}
}