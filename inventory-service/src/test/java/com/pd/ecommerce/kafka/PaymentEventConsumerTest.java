package com.pd.ecommerce.kafka;

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
import static org.mockito.ArgumentMatchers.eq;
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

	private PaymentCompletedEvent event;
	private UUID orderId;


	@BeforeEach
	void setUp() {
		orderId = UUID.randomUUID();
		event = PaymentCompletedEvent.builder()
			.paymentId(UUID.randomUUID())
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
		when(service.reserveInventory(event)).thenReturn(Mono.empty());
		when(eventProducer.sendInventoryReserved(orderId)).thenReturn(Mono.empty());

		StepVerifier.create(consumer.onPaymentCompleted(event)).verifyComplete();

		verify(service).reserveInventory(event);
		verify(eventProducer).sendInventoryReserved(orderId);
		verify(eventProducer, never()).sendInventoryFailed(any(), any());
	}

	@Test
	@DisplayName("onPaymentCompleted - should publish failed event when inventory is insufficient")
	void testOnPaymentCompletedInsufficient() {
		when(service.reserveInventory(event))
			.thenReturn(Mono.error(new InsufficientInventoryException("no stock")));
		when(eventProducer.sendInventoryFailed(eq(orderId), eq("no stock"))).thenReturn(Mono.empty());

		StepVerifier.create(consumer.onPaymentCompleted(event)).verifyComplete();

		verify(eventProducer).sendInventoryFailed(orderId, "no stock");
		verify(eventProducer, never()).sendInventoryReserved(any());
	}
}