package com.pd.ecommerce.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pd.ecommerce.entity.Payment;
import com.pd.ecommerce.entity.PaymentProvider;
import com.pd.ecommerce.entity.PaymentStatus;
import com.pd.ecommerce.event.OrderEventItem;
import com.pd.ecommerce.event.StripeEvent;
import com.pd.ecommerce.exception.PaymentNotFoundException;
import com.pd.ecommerce.kafka.PaymentEventProducer;
import com.pd.ecommerce.providers.StripeWebhookVerifier;
import com.pd.ecommerce.repository.PaymentRepository;
import com.stripe.exception.SignatureVerificationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("StripeWebhookServiceImpl")
class StripeWebhookServiceImplTest {

	@Mock
	private PaymentRepository paymentRepository;

	@Mock
	private PaymentEventProducer paymentEventProducer;

	@Mock
	private ObjectMapper objectMapper;

	@Mock
	private StripeWebhookVerifier verifier;

	@InjectMocks
	private StripeWebhookServiceImpl stripeWebhookService;

	private static final UUID PAYMENT_ID = UUID.randomUUID();
	private static final UUID ORDER_ID = UUID.randomUUID();
	private static final String VALID_PAYLOAD = "{\"type\":\"payment_intent.succeeded\"}";
	private static final String VALID_SIGNATURE = "stripe-sig-abc";

	private Payment pendingPayment;


	@BeforeEach
	void setUp() {
		pendingPayment = Payment.builder().id(PAYMENT_ID).orderId(ORDER_ID).publicOrderId("ORD-PUBLIC-001").userId(UUID.randomUUID()).userMail("user@example.com").amount(new BigDecimal("99.99")).items(List.of(new OrderEventItem(UUID.randomUUID(), "SKU-001", 2))).currency("EUR").status(PaymentStatus.PENDING).provider(PaymentProvider.STRIPE).createdAt(Instant.now()).updatedAt(Instant.now()).build();
	}
	// ── helpers ───────────────────────────────────────────────────────────────

	private StripeEvent successEvent() {
		return new StripeEvent("evt-001", "payment_intent.succeeded", new StripeEvent.Data(new StripeEvent.Object("pi-123", "succeeded", 9999L, "eur", Map.of("paymentId", PAYMENT_ID.toString()))));
	}

	private StripeEvent failureEvent() {
		return new StripeEvent("evt-002", "payment_intent.payment_failed", new StripeEvent.Data(new StripeEvent.Object("pi-123", "requires_payment_method", 9999L, "eur", Map.of("paymentId", PAYMENT_ID.toString()))));
	}

	private StripeEvent unknownEvent() {
		return new StripeEvent("evt-003", "customer.created", new StripeEvent.Data(new StripeEvent.Object("cus-1", null, null, null, Map.of())));
	}
	// ── signature verification ────────────────────────────────────────────────

	@Nested
	@DisplayName("signature verification")
	class SignatureVerification {
		@Test
		@DisplayName("rejects request when signature verification throws")
		void rejectsInvalidSignature() throws Exception {
			when(verifier.verify(VALID_PAYLOAD, "bad-sig")).thenThrow(new SignatureVerificationException("Invalid signature", "bad-sig"));
			StepVerifier.create(stripeWebhookService.handle(VALID_PAYLOAD, "bad-sig")).expectError(SignatureVerificationException.class).verify();
			verify(objectMapper, never()).readValue(any(String.class), eq(StripeEvent.class));
			verify(paymentRepository, never()).findById(any(UUID.class));
		}
	}
	// ── payment_intent.succeeded ──────────────────────────────────────────────

	@Nested
	@DisplayName("payment_intent.succeeded")
	class PaymentSucceeded {
		@Test
		@DisplayName("marks payment COMPLETED and publishes event")
		void completesPaymentAndPublishesEvent() throws Exception {
			when(verifier.verify(VALID_PAYLOAD, VALID_SIGNATURE)).thenReturn(null);
			when(objectMapper.readValue(VALID_PAYLOAD, StripeEvent.class)).thenReturn(successEvent());
			when(paymentRepository.findById(PAYMENT_ID)).thenReturn(Mono.just(pendingPayment));
			when(paymentRepository.updateStatus(eq(PAYMENT_ID), eq(PaymentStatus.COMPLETED), any(Instant.class))).thenReturn(Mono.just(1));
			when(paymentEventProducer.sendPaymentCompleted(any(Payment.class))).thenReturn(Mono.empty());
			StepVerifier.create(stripeWebhookService.handle(VALID_PAYLOAD, VALID_SIGNATURE)).verifyComplete();
			verify(paymentRepository).updateStatus(eq(PAYMENT_ID), eq(PaymentStatus.COMPLETED), any(Instant.class));
			verify(paymentEventProducer).sendPaymentCompleted(any(Payment.class));
		}

		@Test
		@DisplayName("ignores duplicate — skips update and event when already COMPLETED")
		void ignoresDuplicateCompletedEvent() throws Exception {
			pendingPayment.setStatus(PaymentStatus.COMPLETED);
			when(verifier.verify(VALID_PAYLOAD, VALID_SIGNATURE)).thenReturn(null);
			when(objectMapper.readValue(VALID_PAYLOAD, StripeEvent.class)).thenReturn(successEvent());
			when(paymentRepository.findById(PAYMENT_ID)).thenReturn(Mono.just(pendingPayment));
			StepVerifier.create(stripeWebhookService.handle(VALID_PAYLOAD, VALID_SIGNATURE)).verifyComplete();
			verify(paymentRepository, never()).updateStatus(any(UUID.class), any(PaymentStatus.class), any(Instant.class));
			verify(paymentEventProducer, never()).sendPaymentCompleted(any());
		}

		@Test
		@DisplayName("propagates PaymentNotFoundException when payment ID not found")
		void propagatesNotFoundError() throws Exception {
			when(verifier.verify(VALID_PAYLOAD, VALID_SIGNATURE)).thenReturn(null);
			when(objectMapper.readValue(VALID_PAYLOAD, StripeEvent.class)).thenReturn(successEvent());
			when(paymentRepository.findById(PAYMENT_ID)).thenReturn(Mono.empty());
			StepVerifier.create(stripeWebhookService.handle(VALID_PAYLOAD, VALID_SIGNATURE)).expectError(PaymentNotFoundException.class).verify();
			verify(paymentRepository, never()).updateStatus(any(UUID.class), any(PaymentStatus.class), any(Instant.class));
			verify(paymentEventProducer, never()).sendPaymentCompleted(any());
		}

		@Test
		@DisplayName("updates payment object status and timestamp after DB update")
		void updatesPaymentObjectAfterDbUpdate() throws Exception {
			when(verifier.verify(VALID_PAYLOAD, VALID_SIGNATURE)).thenReturn(null);
			when(objectMapper.readValue(VALID_PAYLOAD, StripeEvent.class)).thenReturn(successEvent());
			when(paymentRepository.findById(PAYMENT_ID)).thenReturn(Mono.just(pendingPayment));
			when(paymentRepository.updateStatus(eq(PAYMENT_ID), eq(PaymentStatus.COMPLETED), any(Instant.class))).thenReturn(Mono.just(1));
			when(paymentEventProducer.sendPaymentCompleted(any(Payment.class))).thenReturn(Mono.empty());
			StepVerifier.create(stripeWebhookService.handle(VALID_PAYLOAD, VALID_SIGNATURE)).verifyComplete();
			// The payment object passed to the producer should have COMPLETED status
			assertThat(pendingPayment.getStatus()).isEqualTo(PaymentStatus.COMPLETED);
			assertThat(pendingPayment.getUpdatedAt()).isNotNull();
		}
	}
	// ── payment_intent.payment_failed ─────────────────────────────────────────

	@Nested
	@DisplayName("payment_intent.payment_failed")
	class PaymentFailed {
		private static final String FAILURE_PAYLOAD = "{\"type\":\"payment_intent.payment_failed\"}";

		@Test
		@DisplayName("marks payment FAILED and publishes event")
		void failsPaymentAndPublishesEvent() throws Exception {
			when(verifier.verify(FAILURE_PAYLOAD, VALID_SIGNATURE)).thenReturn(null);
			when(objectMapper.readValue(FAILURE_PAYLOAD, StripeEvent.class)).thenReturn(failureEvent());
			when(paymentRepository.findById(PAYMENT_ID)).thenReturn(Mono.just(pendingPayment));
			when(paymentRepository.updateStatus(eq(PAYMENT_ID), eq(PaymentStatus.FAILED), any(Instant.class))).thenReturn(Mono.just(1));
			when(paymentEventProducer.sendPaymentFailed(any(Payment.class))).thenReturn(Mono.empty());
			StepVerifier.create(stripeWebhookService.handle(FAILURE_PAYLOAD, VALID_SIGNATURE)).verifyComplete();
			verify(paymentRepository).updateStatus(eq(PAYMENT_ID), eq(PaymentStatus.FAILED), any(Instant.class));
			verify(paymentEventProducer).sendPaymentFailed(any(Payment.class));
		}

		@Test
		@DisplayName("skips event publish when updateStatus returns 0 rows (already failed)")
		void skipsEventWhenAlreadyFailed() throws Exception {
			when(verifier.verify(FAILURE_PAYLOAD, VALID_SIGNATURE)).thenReturn(null);
			when(objectMapper.readValue(FAILURE_PAYLOAD, StripeEvent.class)).thenReturn(failureEvent());
			when(paymentRepository.findById(PAYMENT_ID)).thenReturn(Mono.just(pendingPayment));
			when(paymentRepository.updateStatus(eq(PAYMENT_ID), eq(PaymentStatus.FAILED), any(Instant.class))).thenReturn(Mono.just(0));
			StepVerifier.create(stripeWebhookService.handle(FAILURE_PAYLOAD, VALID_SIGNATURE)).verifyComplete();
			verify(paymentEventProducer, never()).sendPaymentFailed(any());
		}

		@Test
		@DisplayName("propagates PaymentNotFoundException when payment ID not found")
		void propagatesNotFoundError() throws Exception {
			when(verifier.verify(FAILURE_PAYLOAD, VALID_SIGNATURE)).thenReturn(null);
			when(objectMapper.readValue(FAILURE_PAYLOAD, StripeEvent.class)).thenReturn(failureEvent());
			when(paymentRepository.findById(PAYMENT_ID)).thenReturn(Mono.empty());
			StepVerifier.create(stripeWebhookService.handle(FAILURE_PAYLOAD, VALID_SIGNATURE)).expectError(PaymentNotFoundException.class).verify();
			verify(paymentRepository, never()).updateStatus(any(UUID.class), any(PaymentStatus.class), any(Instant.class));
			verify(paymentEventProducer, never()).sendPaymentFailed(any());
		}

		@Test
		@DisplayName("updates payment object status and timestamp after DB update")
		void updatesPaymentObjectAfterDbUpdate() throws Exception {
			when(verifier.verify(FAILURE_PAYLOAD, VALID_SIGNATURE)).thenReturn(null);
			when(objectMapper.readValue(FAILURE_PAYLOAD, StripeEvent.class)).thenReturn(failureEvent());
			when(paymentRepository.findById(PAYMENT_ID)).thenReturn(Mono.just(pendingPayment));
			when(paymentRepository.updateStatus(eq(PAYMENT_ID), eq(PaymentStatus.FAILED), any(Instant.class))).thenReturn(Mono.just(1));
			when(paymentEventProducer.sendPaymentFailed(any(Payment.class))).thenReturn(Mono.empty());
			StepVerifier.create(stripeWebhookService.handle(FAILURE_PAYLOAD, VALID_SIGNATURE)).verifyComplete();
			assertThat(pendingPayment.getStatus()).isEqualTo(PaymentStatus.FAILED);
			assertThat(pendingPayment.getUpdatedAt()).isNotNull();
		}
	}
	// ── unknown event types ───────────────────────────────────────────────────

	@Nested
	@DisplayName("unknown event type")
	class UnknownEvent {
		private static final String UNKNOWN_PAYLOAD = "{\"type\":\"customer.created\"}";

		@Test
		@DisplayName("completes without any DB or Kafka interaction for unhandled event types")
		void completesWithoutSideEffectsForUnknownType() throws Exception {
			when(verifier.verify(UNKNOWN_PAYLOAD, VALID_SIGNATURE)).thenReturn(null);
			when(objectMapper.readValue(UNKNOWN_PAYLOAD, StripeEvent.class)).thenReturn(unknownEvent());
			StepVerifier.create(stripeWebhookService.handle(UNKNOWN_PAYLOAD, VALID_SIGNATURE)).verifyComplete();
			verify(paymentRepository, never()).findById(any(UUID.class));
			verify(paymentRepository, never()).updateStatus(any(UUID.class), any(PaymentStatus.class), any(Instant.class));
			verify(paymentEventProducer, never()).sendPaymentCompleted(any());
			verify(paymentEventProducer, never()).sendPaymentFailed(any());
		}
	}
}