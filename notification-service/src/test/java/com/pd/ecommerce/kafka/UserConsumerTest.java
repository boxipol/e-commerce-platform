package com.pd.ecommerce.kafka;

import com.pd.ecommerce.event.UserCreatedEvent;
import com.pd.ecommerce.event.UserDeletedEvent;
import com.pd.ecommerce.service.EmailService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserConsumer Tests")
class UserConsumerTest {

	@Mock
	private EmailService emailService;

	@InjectMocks
	private UserConsumer consumer;


	@Test
	@DisplayName("consume(UserCreatedEvent) - delegates to sendUserCreatedEmail")
	void testConsumeCreated() {
		UserCreatedEvent event = UserCreatedEvent.builder()
			.email("new@example.com")
			.createdAt(Instant.now())
			.build();

		consumer.consume(event);

		verify(emailService).sendUserCreatedEmail(event);
	}

	@Test
	@DisplayName("consume(UserDeletedEvent) - delegates to sendUserDeletedEmail")
	void testConsumeDeleted() {
		UserDeletedEvent event = UserDeletedEvent.builder()
			.email("gone@example.com")
			.createdAt(Instant.now())
			.build();

		consumer.consume(event);

		verify(emailService).sendUserDeletedEmail(event);
	}
}