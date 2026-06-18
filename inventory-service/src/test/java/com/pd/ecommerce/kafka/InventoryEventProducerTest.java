package com.pd.ecommerce.kafka;

import com.pd.ecommerce.event.InventoryReservationCompletedEvent;
import com.pd.ecommerce.event.InventoryReservationFailedEvent;
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
	private UUID productId;


	@BeforeEach
	void setUp() {
		orderId = UUID.randomUUID();
		productId = UUID.randomUUID();
	}

	@Test
	@DisplayName("sendInventoryReserved - should publish completed event to reservation.completed")
	void testSendInventoryReserved() {
		when(kafkaTemplate.send(eq("reservation.completed"), eq(orderId.toString()), any()))
			.thenReturn(CompletableFuture.completedFuture(mock()));

		StepVerifier.create(producer.sendInventoryReserved(orderId)).verifyComplete();

		ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);
		verify(kafkaTemplate).send(eq("reservation.completed"), eq(orderId.toString()), eventCaptor.capture());
		assertThat(eventCaptor.getValue()).isInstanceOf(InventoryReservationCompletedEvent.class);
	}

	@Test
	@DisplayName("sendInventoryFailed - should publish failed event to reservation.failed")
	void testSendInventoryFailed() {
		when(kafkaTemplate.send(eq("reservation.failed"), eq(orderId.toString()), any()))
			.thenReturn(CompletableFuture.completedFuture(mock()));

		StepVerifier.create(producer.sendInventoryFailed(orderId, productId, "out of stock")).verifyComplete();

		ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);
		verify(kafkaTemplate).send(eq("reservation.failed"), eq(orderId.toString()), eventCaptor.capture());
		assertThat(eventCaptor.getValue()).isInstanceOf(InventoryReservationFailedEvent.class);
	}

	@SuppressWarnings("unchecked")
	private SendResult<String, Object> mock() {
		return (SendResult<String, Object>) org.mockito.Mockito.mock(SendResult.class);
	}
}