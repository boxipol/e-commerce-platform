package com.pd.ecommerce.kafka;

import com.pd.ecommerce.event.UserCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public final class UserEventProducer {

	private final KafkaTemplate<String, Object> kafkaTemplate;


	public void sendUserRegistered(UserCreatedEvent event) {
		kafkaTemplate.send(
			"user.created",
			event.email(),
			event
		);
	}
}