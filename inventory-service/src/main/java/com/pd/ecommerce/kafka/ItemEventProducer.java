package com.pd.ecommerce.kafka;

import com.pd.ecommerce.event.ItemUpdateEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public final class ItemEventProducer {

	private final KafkaTemplate<String, Object> kafkaTemplate;


	public void sendItemCreated(ItemUpdateEvent event) {
		kafkaTemplate.send(
			"item.created",
			event.id().toString(),
			event
		);
	}
}