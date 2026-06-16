package com.pd.ecommerce.kafka;

import com.pd.ecommerce.event.InventoryReservationCompletedEvent;
import com.pd.ecommerce.event.InventoryReservationFailedEvent;
import com.pd.ecommerce.service.OrderService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("InventoryEventConsumer")
class InventoryEventConsumerTest {

	@Mock
	private OrderService orderService;

	@InjectMocks
	private InventoryEventConsumer consumer;

	private static final UUID ORDER_ID = UUID.randomUUID();


	@Nested
	@DisplayName("onReservationCompleted")
	class OnReservationCompleted {

		@Test
		@DisplayName("delegates to orderService.markAsPaid and completes")
		void delegatesToMarkAsPaid() {
			InventoryReservationCompletedEvent event = new InventoryReservationCompletedEvent(ORDER_ID);
			when(orderService.markAsPaid(ORDER_ID)).thenReturn(Mono.empty());
			StepVerifier.create(consumer.onReservationCompleted(event)).verifyComplete();
			verify(orderService).markAsPaid(ORDER_ID);
		}

		@Test
		@DisplayName("propagates error from orderService.markAsPaid")
		void propagatesServiceError() {
			InventoryReservationCompletedEvent event = new InventoryReservationCompletedEvent(ORDER_ID);
			when(orderService.markAsPaid(ORDER_ID)).thenReturn(Mono.error(new RuntimeException("DB error")));
			StepVerifier.create(consumer.onReservationCompleted(event)).expectError(RuntimeException.class).verify();
		}
	}

	@Nested
	@DisplayName("onReservationFailed")
	class OnReservationFailed {

		@Test
		@DisplayName("delegates to orderService.markAsFailed and completes")
		void delegatesToMarkAsFailed() {
			InventoryReservationFailedEvent event = new InventoryReservationFailedEvent(ORDER_ID, "Out of stock");
			when(orderService.markAsFailed(ORDER_ID)).thenReturn(Mono.empty());
			StepVerifier.create(consumer.onReservationFailed(event)).verifyComplete();
			verify(orderService).markAsFailed(ORDER_ID);
		}

		@Test
		@DisplayName("propagates error from orderService.markAsFailed")
		void propagatesServiceError() {
			InventoryReservationFailedEvent event = new InventoryReservationFailedEvent(ORDER_ID, "Out of stock");
			when(orderService.markAsFailed(ORDER_ID)).thenReturn(Mono.error(new RuntimeException("DB error")));
			StepVerifier.create(consumer.onReservationFailed(event)).expectError(RuntimeException.class).verify();
		}
	}
}