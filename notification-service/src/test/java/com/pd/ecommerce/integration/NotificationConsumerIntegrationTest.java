package com.pd.ecommerce.integration;

import com.pd.ecommerce.event.OrderCreatedEvent;
import com.pd.ecommerce.event.OrderEventItem;
import com.pd.ecommerce.event.UserCreatedEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;

/**
 * Integration test for the notification-service Kafka consumers running against a real Kafka broker
 * (Testcontainers). Events are published onto the input topics and the test verifies the service
 * consumes them and hands a fully-built email to the (mocked) {@link JavaMailSender}, so no real
 * SMTP server is required.
 */
@SpringBootTest
@DisplayName("Notification Consumer Integration Tests")
class NotificationConsumerIntegrationTest extends AbstractKafkaIntegrationTest {

	@Autowired
	private KafkaTemplate<String, Object> kafkaTemplate;

	/** Replaces the real mail sender so assertions can inspect the outgoing message. */
	@MockitoBean
	private JavaMailSender mailSender;


	@Test
	@DisplayName("user.created - should send a registration email to the new user")
	void testUserCreatedTriggersEmail() {
		UserCreatedEvent event = UserCreatedEvent.builder()
			.email("newuser@example.com")
			.createdAt(Instant.now())
			.build();

		kafkaTemplate.send("user.created", event.email(), event);

		await().atMost(Duration.ofSeconds(30)).untilAsserted(() -> {
			var captor = forClass(SimpleMailMessage.class);
			verify(mailSender, atLeastOnce()).send(captor.capture());
			assertThat(captor.getAllValues())
				.anySatisfy(message -> {
					assertThat(message.getTo()).containsExactly("newuser@example.com");
					assertThat(message.getSubject()).contains("User Confirmation");
				});
		});
	}

	@Test
	@DisplayName("order.created - should send an order confirmation email to the buyer")
	void testOrderCreatedTriggersEmail() {
		UUID orderId = UUID.randomUUID();
		OrderCreatedEvent event = OrderCreatedEvent.builder()
			.orderId(orderId)
			.publicOrderId("ORD-" + orderId)
			.userId(UUID.randomUUID())
			.userMail("buyer@example.com")
			.items(List.of(new OrderEventItem("SKU-1", 2)))
			.totalPrice(new BigDecimal("199.98"))
			.build();

		kafkaTemplate.send("order.created", event.publicOrderId(), event);

		await().atMost(Duration.ofSeconds(30)).untilAsserted(() -> {
			var captor = forClass(SimpleMailMessage.class);
			verify(mailSender, atLeastOnce()).send(captor.capture());
			assertThat(captor.getAllValues())
				.anySatisfy(message -> {
					assertThat(message.getTo()).containsExactly("buyer@example.com");
					assertThat(message.getSubject()).contains("Order Confirmation");
					assertThat(message.getText()).contains("ORD-" + orderId);
				});
		});
	}
}
