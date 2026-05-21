package com.pd.ecommerce.kafka;

import com.pd.ecommerce.event.UserCreatedEvent;
import com.pd.ecommerce.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public final class UserConsumer {

	private final EmailService emailService;


	@KafkaListener(topics = "user.created", groupId = "notification-group")
	public void consume(UserCreatedEvent event) {
		log.info("Received user created event: {}", event);
		log.info("Sending notification for user {}", event.email());

		emailService.sendUserCreatedEmail(event);
	}
}