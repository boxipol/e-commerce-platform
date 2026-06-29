package com.pd.ecommerce.kafka;

import com.pd.ecommerce.event.InventoryReservationCompletedEvent;
import com.pd.ecommerce.event.InventoryReservationFailedEvent;
import com.pd.ecommerce.event.OrderEventItem;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import reactor.test.StepVerifier;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("InventoryEventProducer Tests")
class InventoryEventProducerTest {

	@Mock
	private KafkaTemplate<String, Object> kafkaTemplate;

	@InjectMocks
	private InventoryEventProducer producer;

	private UUID orderId;


	@BeforeEach
	void setUp() {
		orderId = UUID.randomUUID();
	}

	@Test
	@DisplayName("sendInventoryReserved - should publish completed event to reservation.completed")
	void testSendInventoryReserved() {
		when(kafkaTemplate.send(eq("reservation.completed"), eq(orderId.toString()), any()))
			.thenReturn(CompletableFuture.completedFuture(mockSendResult("reservation.completed")));

		List<OrderEventItem> items = List.of(new OrderEventItem(UUID.randomUUID(), "SKU-1", 2));
		StepVerifier.create(producer.sendInventoryReserved(orderId, items)).verifyComplete();

		ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);
		verify(kafkaTemplate).send(eq("reservation.completed"), eq(orderId.toString()), eventCaptor.capture());
		assertThat(eventCaptor.getValue()).isInstanceOf(InventoryReservationCompletedEvent.class);
		InventoryReservationCompletedEvent event = (InventoryReservationCompletedEvent) eventCaptor.getValue();
		assertThat(event.items()).hasSize(1);
	}

	@Test
	@DisplayName("sendInventoryFailed - should publish failed event to reservation.failed")
	void testSendInventoryFailed() {
		when(kafkaTemplate.send(eq("reservation.failed"), eq(orderId.toString()), any()))
			.thenReturn(CompletableFuture.completedFuture(mockSendResult("reservation.failed")));

		InventoryReservationFailedEvent event = InventoryReservationFailedEvent.builder()
			.orderId(orderId)
			.publicOrderId("ORD-1")
			.paymentId(UUID.randomUUID())
			.userMail("buyer@example.com")
			.reason("out of stock")
			.build();

		StepVerifier.create(producer.sendInventoryFailed(event)).verifyComplete();

		ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);
		verify(kafkaTemplate).send(eq("reservation.failed"), eq(orderId.toString()), eventCaptor.capture());
		assertThat(eventCaptor.getValue()).isInstanceOf(InventoryReservationFailedEvent.class);
	}

	private SendResult<String, Object> mockSendResult(String topic) {
		RecordMetadata metadata = new RecordMetadata(
			new TopicPartition(topic, 0),
			0,
			1,
			System.currentTimeMillis(),
			0,
			0
		);
		return new SendResult<>(null, metadata);
	}
}