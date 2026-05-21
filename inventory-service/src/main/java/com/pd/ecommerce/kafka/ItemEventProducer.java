package com.pd.ecommerce.kafka;

import com.pd.ecommerce.event.ItemUpdateEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public final class ItemEventProducer {

	private final KafkaTemplate<String, Object> kafkaTemplate;


	public void sendUserRegistered(ItemUpdateEvent event) {
		kafkaTemplate.send(
			"user.created",
			event.id().toString(),
			event
		);
	}
}