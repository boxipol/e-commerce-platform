package com.pd.ecommerce.service;

import com.pd.ecommerce.dto.CreatePaymentRequest;
import com.pd.ecommerce.dto.PaymentResponse;
import com.pd.ecommerce.entity.Payment;
import com.pd.ecommerce.entity.PaymentStatus;
import com.pd.ecommerce.event.OrderCreatedEvent;
import com.pd.ecommerce.kafka.PaymentEventProducer;
import com.pd.ecommerce.mapper.PaymentMapper;
import com.pd.ecommerce.repository.PaymentRepository;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import java.time.Instant;
import java.util.Random;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public final class PaymentServiceImpl implements PaymentService {

	private final PaymentRepository repository;
	private final PaymentEventProducer eventProducer;
	private final PaymentMapper mapper;


//	 TEST
	public Mono<PaymentResponse> createPayment(CreatePaymentRequest request) {
		Payment payment = mapper.toEntity(request);
		payment.setProvider("STRIPE");
		payment.setStatus(PaymentStatus.PENDING);
		payment.setCreatedAt(Instant.now());
		payment.setUpdatedAt(Instant.now());

		return repository.save(payment)
			.flatMap(this::processPayment)
			.map(mapper::toResponse);
	}

	// TEST
	public Mono<PaymentResponse> createPayment(OrderCreatedEvent event) {
		Payment payment = Payment.builder()
			.orderId(event.orderId())
			.userId(event.userId())
			.amount(event.totalPrice())
			.currency("EUR")
			.status(PaymentStatus.PENDING)
			.provider("STRIPE")
			.createdAt(Instant.now())
			.updatedAt(Instant.now())
			.build();

		return repository.save(payment)
			.flatMap(this::processPayment)
			.map(mapper::toResponse);
	}

	public Mono<PaymentResponse> createPayment2(CreatePaymentRequest request) {
		return Mono.fromCallable(() -> {
				PaymentIntentCreateParams params =
					PaymentIntentCreateParams.builder()
						.setAmount(request.amount().longValue())
						.setCurrency(request.currency())
						.putMetadata("orderId", request.orderId().toString())
						.setAutomaticPaymentMethods(
							PaymentIntentCreateParams.AutomaticPaymentMethods
								.builder()
								.setEnabled(true)
								.build()
						)
						.build();

				return PaymentIntent.create(params);
			})
			.subscribeOn(Schedulers.boundedElastic())
			.flatMap(intent -> {
				Payment payment = Payment.builder()
					.orderId(request.orderId())
					.provider("STRIPE")
					.id(UUID.fromString(intent.getId()))
//					.clientSecret(intent.getClientSecret())
					.status(PaymentStatus.PENDING)
					.amount(request.amount())
					.currency(request.currency())
					.build();

				return repository.save(payment);
			})
			.map(mapper::toResponse);
	}

//	==================== PRIVATE ====================

	private Mono<Payment> processPayment(Payment payment) {
		boolean success = new Random().nextBoolean();

		payment.setStatus(
			success
				? PaymentStatus.SUCCEEDED
				: PaymentStatus.FAILED
		);

		payment.setUpdatedAt(Instant.now());

		return repository.save(payment)
			.doOnSuccess(saved -> {
				if (saved.getStatus() == PaymentStatus.SUCCEEDED) {
					eventProducer.sendPaymentCompleted(saved);
				} else {
					eventProducer.sendPaymentFailed(saved);
				}
			});
	}
}