package com.pd.ecommerce.service;

import com.stripe.model.Event;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
final class WebhookServiceImpl implements WebhookService {

	private final PaymentService paymentService;


	@Override
	public Mono<ResponseEntity<Void>> handleWebhook(String payload, String signature) {
//		return Mono.fromCallable(() -> {
//				Event event = Webhook.constructEvent(
//					payload,
//					signature,
//					stripeWebhookSecret
//				);
//
//				return event;
//			})
//			.subscribeOn(Schedulers.boundedElastic());

		return Mono.empty();
	}

	public Mono<Void> handle(Event event) {
//		return switch (event.getType()) {
//			case "payment_intent.succeeded" ->
//				handleSuccess(event);
//			case "payment_intent.payment_failed" ->
//				handleFailure(event);
//
//			default -> Mono.empty();
//		};

		return Mono.empty();
	}

//	private Mono<Void> handleSuccess(Event event) {
//		PaymentIntent intent =
//			(PaymentIntent) event.getDataObjectDeserializer()
//				.getObject()
//				.orElseThrow();
//
//		return repository.findByProviderPaymentId(intent.getId())
//			.flatMap(payment -> {
//
//				payment.setStatus(PaymentStatus.SUCCEEDED);
//				return repository.save(payment);
//			})
//			.then();
//	}
//
//	private Mono<Void> handleFailure(Event event) {
//		PaymentIntent intent =
//			(PaymentIntent) event.getDataObjectDeserializer()
//				.getObject()
//				.orElseThrow();
//
//		return repository.findByProviderPaymentId(intent.getId())
//			.flatMap(payment -> {
//
//				payment.setStatus(PaymentStatus.SUCCEEDED);
//				return repository.save(payment);
//			})
//			.then();
//	}
}
