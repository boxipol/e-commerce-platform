package com.pd.ecommerce.kafka;

import com.pd.ecommerce.event.UserCreatedEvent;
import com.pd.ecommerce.event.UserDeletedEvent;
import com.pd.ecommerce.event.UserUpdatedEvent;
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

	public void sendUserUpdated(UserUpdatedEvent event) {
		kafkaTemplate.send(
			"user.updated",
			event.email(),
			event
		);
	}

	public void sendUserDeleted(UserDeletedEvent event) {
		kafkaTemplate.send(
			"user.deleted",
			event.email(),
			event
		);
	}
}