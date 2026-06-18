package com.pd.ecommerce.kafka;

import com.pd.ecommerce.event.InventoryReservationFailedEvent;
import com.pd.ecommerce.event.OrderEventItem;
import com.pd.ecommerce.event.PaymentCompletedEvent;
import com.pd.ecommerce.exception.InsufficientInventoryException;
import com.pd.ecommerce.service.InventoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Inventory PaymentEventConsumer Tests")
class PaymentEventConsumerTest {

	@Mock
	private InventoryService service;

	@Mock
	private InventoryEventProducer eventProducer;

	@InjectMocks
	private PaymentEventConsumer consumer;

	private PaymentCompletedEvent completedEvent;

	private UUID orderId;


	@BeforeEach
	void setUp() {
		orderId = UUID.randomUUID();
		UUID paymentId = UUID.randomUUID();

		completedEvent = PaymentCompletedEvent.builder()
			.paymentId(paymentId)
			.userMail("buyer@example.com")
			.orderId(orderId)
			.publicOrderId("ORD-1")
			.items(List.of(new OrderEventItem(UUID.randomUUID(), "SKU-1", 1)))
			.amount(BigDecimal.TEN)
			.build();
	}

	@Test
	@DisplayName("onPaymentCompleted - should reserve inventory then publish reserved event")
	void testOnPaymentCompletedSuccess() {
		when(service.reserveInventory(completedEvent)).thenReturn(Mono.empty());
		when(eventProducer.sendInventoryReserved(orderId)).thenReturn(Mono.empty());

		StepVerifier.create(consumer.onPaymentCompleted(completedEvent)).verifyComplete();

		verify(service).reserveInventory(completedEvent);
		verify(eventProducer).sendInventoryReserved(orderId);
		verify(eventProducer, never()).sendInventoryFailed(any());
	}

	@Test
	@DisplayName("onPaymentCompleted - should publish failed event when inventory is insufficient")
	void testOnPaymentCompletedInsufficient() {
		when(service.reserveInventory(completedEvent))
			.thenReturn(Mono.error(new InsufficientInventoryException("no stock")));

		InventoryReservationFailedEvent failedEvent = InventoryReservationFailedEvent.builder()
			.orderId(completedEvent.orderId())
			.publicOrderId(completedEvent.publicOrderId())
			.paymentId(completedEvent.paymentId())
			.userMail(completedEvent.userMail())
			.reason("no stock")
			.build();

		when(eventProducer.sendInventoryFailed(failedEvent)).thenReturn(Mono.empty());

		StepVerifier.create(consumer.onPaymentCompleted(completedEvent)).verifyComplete();

		verify(eventProducer).sendInventoryFailed(failedEvent);
		verify(eventProducer, never()).sendInventoryReserved(any());
	}
}