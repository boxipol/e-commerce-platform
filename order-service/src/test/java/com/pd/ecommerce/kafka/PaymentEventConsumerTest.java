package com.pd.ecommerce.kafka;

import com.pd.ecommerce.event.PaymentFailedEvent;
import com.pd.ecommerce.service.OrderService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentEventConsumer")
class PaymentEventConsumerTest {

	private static final UUID ORDER_ID = UUID.randomUUID();

	@Mock
	private OrderService orderService;

	@InjectMocks
	private PaymentEventConsumer consumer;


	@Test
	@DisplayName("onPaymentFailed - delegates to orderService.markAsFailed and completes")
	void delegatesToMarkAsFailed() {
		PaymentFailedEvent event = PaymentFailedEvent.builder().paymentId(UUID.randomUUID()).orderId(ORDER_ID).publicOrderId("ORD-ABCD1234").userMail("user@example.com").amount(new BigDecimal("99.99")).build();
		when(orderService.markAsFailed(ORDER_ID)).thenReturn(Mono.empty());
		StepVerifier.create(consumer.onPaymentFailed(event)).verifyComplete();
		verify(orderService).markAsFailed(ORDER_ID);
	}

	@Test
	@DisplayName("onPaymentFailed - propagates error from orderService")
	void propagatesServiceError() {
		PaymentFailedEvent event = PaymentFailedEvent.builder().paymentId(UUID.randomUUID()).orderId(ORDER_ID).publicOrderId("ORD-ABCD1234").userMail("user@example.com").amount(new BigDecimal("99.99")).build();
		when(orderService.markAsFailed(ORDER_ID)).thenReturn(Mono.error(new RuntimeException("DB error")));
		StepVerifier.create(consumer.onPaymentFailed(event)).expectError(RuntimeException.class).verify();
	}
}