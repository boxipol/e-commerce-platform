package com.pd.ecommerce.kafka;

import com.pd.ecommerce.event.InventoryReservationCompletedEvent;
import com.pd.ecommerce.event.OrderEventItem;
import com.pd.ecommerce.service.ProductStockSyncService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("InventoryReservationCompletedConsumer")
class InventoryReservationCompletedConsumerTest {

	@Mock
	private ProductStockSyncService productStockSyncService;

	@InjectMocks
	private InventoryReservationCompletedConsumer consumer;

	@Test
	@DisplayName("onReservationCompleted - should delegate to stock sync service")
	void onReservationCompleted_success() {
		UUID orderId = UUID.randomUUID();
		List<OrderEventItem> items = List.of(new OrderEventItem(UUID.randomUUID(), "SKU-1", 2));
		InventoryReservationCompletedEvent event = new InventoryReservationCompletedEvent(orderId, items);

		when(productStockSyncService.decreaseStockForReservation(orderId, items)).thenReturn(Mono.empty());

		StepVerifier.create(consumer.onReservationCompleted(event))
			.verifyComplete();

		verify(productStockSyncService).decreaseStockForReservation(orderId, items);
	}

	@Test
	@DisplayName("onReservationCompleted - should propagate sync failures")
	void onReservationCompleted_error() {
		UUID orderId = UUID.randomUUID();
		List<OrderEventItem> items = List.of(new OrderEventItem(UUID.randomUUID(), "SKU-1", 2));
		InventoryReservationCompletedEvent event = new InventoryReservationCompletedEvent(orderId, items);

		when(productStockSyncService.decreaseStockForReservation(orderId, items))
			.thenReturn(Mono.error(new RuntimeException("sync failed")));

		StepVerifier.create(consumer.onReservationCompleted(event))
			.expectError(RuntimeException.class)
			.verify();
	}
}
