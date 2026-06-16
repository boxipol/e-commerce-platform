package com.pd.ecommerce.kafka;

import com.pd.ecommerce.entity.Payment;
import com.pd.ecommerce.event.PaymentCompletedEvent;
import com.pd.ecommerce.event.PaymentFailedEvent;
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

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentEventProducer Tests")
class PaymentEventProducerTest {

	@Mock
	private KafkaTemplate<String, Object> kafkaTemplate;

	@InjectMocks
	private PaymentEventProducer producer;

	private Payment payment;
	private UUID orderId;


	@BeforeEach
	void setUp() {
		orderId = UUID.randomUUID();
		payment = Payment.builder()
			.id(UUID.randomUUID())
			.orderId(orderId)
			.publicOrderId("ORD-1")
			.userMail("buyer@example.com")
			.amount(BigDecimal.TEN)
			.items(List.of())
			.build();
	}

	@Test
	@DisplayName("sendPaymentCompleted - should publish completed event keyed by order id")
	void testSendPaymentCompleted() {
		when(kafkaTemplate.send(eq("payment.completed"), eq(orderId.toString()), any()))
			.thenReturn(CompletableFuture.completedFuture(sendResult()));

		StepVerifier.create(producer.sendPaymentCompleted(payment)).verifyComplete();

		ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
		verify(kafkaTemplate).send(eq("payment.completed"), eq(orderId.toString()), captor.capture());
		assertThat(captor.getValue()).isInstanceOf(PaymentCompletedEvent.class);
	}

	@Test
	@DisplayName("sendPaymentFailed - should publish failed event keyed by order id")
	void testSendPaymentFailed() {
		when(kafkaTemplate.send(eq("payment.failed"), eq(orderId.toString()), any()))
			.thenReturn(CompletableFuture.completedFuture(sendResult()));

		StepVerifier.create(producer.sendPaymentFailed(payment)).verifyComplete();

		ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
		verify(kafkaTemplate).send(eq("payment.failed"), eq(orderId.toString()), captor.capture());
		assertThat(captor.getValue()).isInstanceOf(PaymentFailedEvent.class);
	}

	@SuppressWarnings("unchecked")
	private SendResult<String, Object> sendResult() {
		return (SendResult<String, Object>) org.mockito.Mockito.mock(SendResult.class);
	}
}