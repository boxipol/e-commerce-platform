package com.pd.ecommerce.kafka;

import com.pd.ecommerce.dto.PaymentResponse;
import com.pd.ecommerce.event.OrderCreatedEvent;
import com.pd.ecommerce.service.PaymentService;
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

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderCreatedConsumer Tests")
class OrderCreatedConsumerTest {

	@Mock
	private PaymentService paymentService;

	@InjectMocks
	private OrderCreatedConsumer consumer;


	private OrderCreatedEvent event() {
		return OrderCreatedEvent.builder()
			.orderId(UUID.randomUUID())
			.publicOrderId("ORD-1")
			.userId(UUID.randomUUID())
			.userMail("buyer@example.com")
			.items(List.of())
			.totalPrice(BigDecimal.TEN)
			.build();
	}

	@Test
	@DisplayName("consume - should complete when payment creation succeeds")
	void testConsumeSuccess() {
		OrderCreatedEvent event = event();
		when(paymentService.createPayment(event)).thenReturn(Mono.just(PaymentResponse.builder().build()));

		StepVerifier.create(consumer.consume(event))
			.verifyComplete();

		verify(paymentService).createPayment(event);
	}

	@Test
	@DisplayName("consume - should propagate error so Spring Kafka can route to DLT")
	void testConsumeError() {
		OrderCreatedEvent event = event();
		when(paymentService.createPayment(event)).thenReturn(Mono.error(new RuntimeException("boom")));

		StepVerifier.create(consumer.consume(event))
			.expectError(RuntimeException.class)
			.verify();

		verify(paymentService).createPayment(event);
	}
}
