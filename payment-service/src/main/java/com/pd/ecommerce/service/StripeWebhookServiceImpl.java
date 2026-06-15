package com.pd.ecommerce.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pd.ecommerce.entity.Payment;
import com.pd.ecommerce.entity.PaymentStatus;
import com.pd.ecommerce.event.StripeEvent;
import com.pd.ecommerce.exception.PaymentNotFoundException;
import com.pd.ecommerce.kafka.PaymentEventProducer;
import com.pd.ecommerce.providers.StripeWebhookVerifier;
import com.pd.ecommerce.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
final class StripeWebhookServiceImpl implements StripeWebhookService {

	private final PaymentRepository paymentRepository;
	private final PaymentEventProducer paymentEventProducer;
	private final ObjectMapper objectMapper;
	private final StripeWebhookVerifier verifier;


	@Override
	public Mono<Void> handle(String payload, String signature) {
		return Mono.fromCallable(() ->
				verifier.verify(payload, signature)
			)
			.then(Mono.fromCallable(() ->
				objectMapper.readValue(payload, StripeEvent.class)
			))
			.doOnNext(event ->
				log.info("Received Stripe event type={} id={}", event.type(), event.id())
			)
			.flatMap(event ->
				routeEvent(event).doOnSuccess(unused ->
					log.info("Processed Stripe event type={} id={}", event.type(), event.id())
				)
			)
			.doOnError(ex ->
				log.error("Stripe webhook processing failed", ex)
			)
			.then();
	}

//	==================== PRIVATE ====================

	private Mono<Void> routeEvent(StripeEvent event) {
		return switch (event.type()) {
			case "payment_intent.succeeded" ->
				handleSuccess(event);
			case "payment_intent.payment_failed" ->
				handleFailure(event);
			default ->
				Mono.empty();
		};
	}

	private Mono<Void> handleSuccess(StripeEvent event) {
		String paymentId = event.data()
			.object()
			.metadata()
			.get("paymentId");

		log.info("Processing successful payment webhook paymentId={}", paymentId);

		return findPaymentById(paymentId)
			.flatMap(this::completePayment)
			.doOnError(PaymentNotFoundException.class, ex ->
				log.error("Stripe webhook orphan_event payment_not_found providerPaymentId={}", paymentId)
			)
			.then();
	}

	private Mono<Void> handleFailure(StripeEvent event) {
		String paymentId = event.data().object().metadata().get("paymentId");

		log.info("stripe.webhook.failure received providerPaymentId={}", paymentId);

		return findPaymentById(paymentId)
			.flatMap(this::failPayment)
			.doOnError(PaymentNotFoundException.class, ex ->
				log.error("Stripe webhook failure orphan_event payment_not_found providerPaymentId={}", paymentId)
			)
			.then();
	}

	private Mono<Payment> findPaymentById(String paymentId) {
		return paymentRepository.findById(UUID.fromString(paymentId))
			.switchIfEmpty(Mono.error(new PaymentNotFoundException(paymentId)));
	}

	private Mono<Void> completePayment(Payment payment) {
		if (payment.getStatus() == PaymentStatus.COMPLETED) {
			log.info("Stripe webhook duplicate ignored paymentId={}", payment.getId());
			return Mono.empty();
		}

		log.info("Stripe webhook mark_completed paymentId={}", payment.getId());

		Instant updatedAt = Instant.now();

		return paymentRepository.updateStatus(payment.getId(), PaymentStatus.COMPLETED, updatedAt)
			.doOnNext(rows ->
				log.info("Stripe webhook saved_completed paymentId={}", payment.getId())
			)
			.then(Mono.fromRunnable(() -> {
				payment.setStatus(PaymentStatus.COMPLETED);
				payment.setUpdatedAt(updatedAt);
			}))
			.then(paymentEventProducer.sendPaymentCompleted(payment))
			.doOnSuccess(v ->
				log.info("Stripe webhook event_published paymentId={}", payment.getId())
			);
	}

	private Mono<Void> failPayment(Payment payment) {
		Instant updatedAt = Instant.now();

		return paymentRepository.updateStatus(payment.getId(), PaymentStatus.FAILED, updatedAt)
			.flatMap(rows -> {
				if (rows == 0) {
					log.info(
						"Stripe webhook failure already_failed paymentId={}",
						payment.getId()
					);

					return Mono.empty();
				}

				payment.setStatus(PaymentStatus.FAILED);
				payment.setUpdatedAt(updatedAt);

				log.info("Stripe webhook failure saved_failed paymentId={}", payment.getId());

				return paymentEventProducer.sendPaymentFailed(payment)
					.doOnSuccess(v ->
						log.info("Stripe webhook failure event_published paymentId={}", payment.getId())
					);
			});
	}
}