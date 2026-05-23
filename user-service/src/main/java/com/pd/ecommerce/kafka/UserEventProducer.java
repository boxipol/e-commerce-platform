package com.pd.ecommerce.kafka;

import com.pd.ecommerce.event.UserCreatedEvent;
import com.pd.ecommerce.event.UserDeletedEvent;
import com.pd.ecommerce.event.UserUpdatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
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

		log.info("User created: {}", event);
	}

	public void sendUserUpdated(UserUpdatedEvent event) {
		kafkaTemplate.send(
			"user.updated",
			event.email(),
			event
		);

		log.info("User updated: {}", event);
	}

	public void sendUserDeleted(UserDeletedEvent event) {
		kafkaTemplate.send(
			"user.deleted",
			event.email(),
			event
		);
	}
}