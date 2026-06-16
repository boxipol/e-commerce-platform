package com.pd.ecommerce.kafka;

import com.pd.ecommerce.event.PaymentCompletedEvent;
import com.pd.ecommerce.event.PaymentFailedEvent;
import com.pd.ecommerce.service.EmailService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentConsumer Tests")
class PaymentConsumerTest {

	@Mock
	private EmailService emailService;

	@InjectMocks
	private PaymentConsumer consumer;


	@Test
	@DisplayName("consume(PaymentCompletedEvent) - delegates to sendPaymentCompletedEmail")
	void testConsumeCompleted() {
		PaymentCompletedEvent event = PaymentCompletedEvent.builder()
			.paymentId(UUID.randomUUID())
			.userMail("buyer@example.com")
			.orderId(UUID.randomUUID())
			.publicOrderId("ORD-1")
			.items(List.of())
			.amount(BigDecimal.TEN)
			.build();

		consumer.consume(event);

		verify(emailService).sendPaymentCompletedEmail(event);
	}

	@Test
	@DisplayName("consume(PaymentFailedEvent) - delegates to sendPaymentFailedEmail")
	void testConsumeFailed() {
		PaymentFailedEvent event = PaymentFailedEvent.builder()
			.paymentId(UUID.randomUUID())
			.userMail("buyer@example.com")
			.orderId(UUID.randomUUID())
			.publicOrderId("ORD-1")
			.amount(BigDecimal.TEN)
			.build();

		consumer.consume(event);

		verify(emailService).sendPaymentFailedEmail(event);
	}
}