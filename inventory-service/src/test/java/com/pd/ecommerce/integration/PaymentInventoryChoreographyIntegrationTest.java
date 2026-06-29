package com.pd.ecommerce.integration;

import com.pd.ecommerce.event.InventoryReservationCompletedEvent;
import com.pd.ecommerce.event.InventoryReservationFailedEvent;
import com.pd.ecommerce.event.OrderEventItem;
import com.pd.ecommerce.event.PaymentCompletedEvent;
import com.pd.ecommerce.repository.InventoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

/**
 * End-to-end integration test for the inventory slice of the
 * order → payment → inventory choreography.
 *
 * <p>Boots the full inventory-service Spring context against a real Kafka broker and a real
 * PostgreSQL database (Testcontainers). A {@code payment.completed} event is published onto Kafka;
 * the service consumes it, reserves stock in the database, and emits a follow-up reservation event.
 * The test asserts both the database side effect and the outbound Kafka event.
 */
@SpringBootTest
@DisplayName("Payment → Inventory Choreography Integration Tests")
class PaymentInventoryChoreographyIntegrationTest extends AbstractKafkaPostgresIntegrationTest {

	@Autowired
	private KafkaTemplate<String, Object> kafkaTemplate;

	@Autowired
	private InventoryRepository repository;

	@Autowired
	private ReservationEventRecorder recorder;

	private UUID productId;

	@BeforeEach
	void setUp() {
		productId = UUID.randomUUID();
		Instant now = Instant.now();

		repository.deleteAll()
			.then(repository.insert(productId, 10, now, now))
			.block();

		recorder.completed.clear();
		recorder.failed.clear();
	}

	@Test
	@DisplayName("payment.completed with sufficient stock - deducts inventory and emits reservation.completed")
	void testReservationSucceeds() {
		UUID orderId = UUID.randomUUID();
		PaymentCompletedEvent event = paymentEvent(orderId, 3);

		kafkaTemplate.send("payment.completed", orderId.toString(), event);

		// Outbound success event is published with the originating order id.
		await().atMost(Duration.ofSeconds(30)).untilAsserted(() ->
			assertThat(recorder.completed).extracting(InventoryReservationCompletedEvent::orderId)
				.contains(orderId));
		await().atMost(Duration.ofSeconds(30)).untilAsserted(() ->
			assertThat(recorder.completed)
				.anySatisfy(completed -> {
					assertThat(completed.orderId()).isEqualTo(orderId);
					assertThat(completed.items()).hasSize(1);
					assertThat(completed.items().get(0).sku()).isEqualTo("SKU-1");
					assertThat(completed.items().get(0).quantity()).isEqualTo(3);
				}));

		// Stock was decremented in the database.
		await().atMost(Duration.ofSeconds(10)).untilAsserted(() ->
			assertThat(repository.findById(productId).block().getQuantity()).isEqualTo(7));

		assertThat(recorder.failed).isEmpty();
	}

	@Test
	@DisplayName("payment.completed with insufficient stock - leaves inventory and emits reservation.failed")
	void testReservationFails() {
		UUID orderId = UUID.randomUUID();
		PaymentCompletedEvent event = paymentEvent(orderId, 99);

		kafkaTemplate.send("payment.completed", orderId.toString(), event);

		await().atMost(Duration.ofSeconds(30)).untilAsserted(() ->
			assertThat(recorder.failed).extracting(InventoryReservationFailedEvent::orderId)
				.contains(orderId));

		// Stock must remain untouched to prevent overselling.
		assertThat(repository.findById(productId).block().getQuantity()).isEqualTo(10);
		assertThat(recorder.completed).isEmpty();
	}

	private PaymentCompletedEvent paymentEvent(UUID orderId, int quantity) {
		return PaymentCompletedEvent.builder()
			.paymentId(UUID.randomUUID())
			.userMail("buyer@example.com")
			.orderId(orderId)
			.publicOrderId("ORD-" + orderId)
			.items(List.of(new OrderEventItem(productId, "SKU-1", quantity)))
			.amount(BigDecimal.TEN)
			.build();
	}

	/**
	 * Test-only consumer that captures the reservation events the service emits, so assertions can
	 * verify the outbound side of the choreography.
	 */
	@TestConfiguration
	static class TestKafkaConfig {

		@Bean
		ReservationEventRecorder reservationEventRecorder() {
			return new ReservationEventRecorder();
		}
	}

	static class ReservationEventRecorder {

		final BlockingQueue<InventoryReservationCompletedEvent> completed = new LinkedBlockingQueue<>();
		final BlockingQueue<InventoryReservationFailedEvent> failed = new LinkedBlockingQueue<>();

		@KafkaListener(topics = "reservation.completed", groupId = "it-recorder")
		void onCompleted(InventoryReservationCompletedEvent event) {
			completed.add(event);
		}

		@KafkaListener(topics = "reservation.failed", groupId = "it-recorder")
		void onFailed(InventoryReservationFailedEvent event) {
			failed.add(event);
		}
	}
}
