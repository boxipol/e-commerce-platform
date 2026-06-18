package com.pd.ecommerce.service;

import com.pd.ecommerce.dto.CreateProviderPaymentRequest;
import com.pd.ecommerce.dto.PaymentResponse;
import com.pd.ecommerce.dto.ProviderPaymentResponse;
import com.pd.ecommerce.entity.Payment;
import com.pd.ecommerce.entity.PaymentProvider;
import com.pd.ecommerce.entity.PaymentStatus;
import com.pd.ecommerce.event.OrderCreatedEvent;
import com.pd.ecommerce.providers.PaymentProviderRegistry;
import com.pd.ecommerce.providers.PaymentProviderService;
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
public final class PaymentServiceImpl implements PaymentService {

	private final PaymentRepository repository;
	private final PaymentProviderRegistry paymentProviderRegistry;


	public Mono<PaymentResponse> createPayment(OrderCreatedEvent event) {
		PaymentProviderService paymentProvider = paymentProviderRegistry.get(resolveProvider());
		Instant createdAt = Instant.now();

		Payment payment = Payment.builder()
			.orderId(event.orderId())
			.publicOrderId(event.publicOrderId())
			.userId(event.userId())
			.userMail(event.userMail())
			.amount(event.totalPrice())
			.items(event.items())
			.currency("EUR") // todo add ccy provider
			.status(PaymentStatus.PENDING)
			.provider(paymentProvider.provider())
			.createdAt(createdAt)
			.updatedAt(createdAt)
			.build();

		return repository.save(payment)
			.flatMap(saved ->
				paymentProvider.createPayment(toProviderRequest(saved))
					.flatMap(response ->
						updatePayment(saved, response)
					)
			)
			.doOnSuccess(response ->
				log.info("Payment {} created successfully for order {}", response.id(), payment.getOrderId())
			)
			.doOnError(ex ->
				log.error("Failed to create payment for order {}", payment.getOrderId(), ex)
			)
			.map(response -> toResponse(payment, response));
	}

	@Override
	public Mono<Void> markForRefund(UUID paymentId) {
		repository.updateStatus(paymentId, PaymentStatus.REFUNDING, Instant.now())
			.flatMap(rows -> {
				if (rows == 0) {
					log.info("Failed to mark paymentId: {}, for refund!", paymentId);
				}

				return Mono.empty();
			});

		return Mono.empty();
	}

//	==================== PRIVATE ====================

	// todo resolve based on some logic
	private PaymentProvider resolveProvider() {
		return PaymentProvider.STRIPE;
//		boolean success = new Random().nextBoolean();
//
//		return success
//			? PaymentProvider.STRIPE
//			: PaymentProvider.PAYPAL;
	}

	private CreateProviderPaymentRequest toProviderRequest(Payment payment) {
		return CreateProviderPaymentRequest.builder()
			.paymentId(payment.getId())
			.orderId(payment.getOrderId())
			.amount(payment.getAmount())
			.currency(payment.getCurrency())
			.build();
	}

	private Mono<ProviderPaymentResponse> updatePayment(Payment payment, ProviderPaymentResponse response) {
		return repository.updateProviderData(payment.getId(), response.id(), response.url(), Instant.now())
			.thenReturn(response);
	}

	private PaymentResponse toResponse(Payment payment, ProviderPaymentResponse response) {
		return PaymentResponse.builder()
			.id(payment.getId())
			.orderId(payment.getOrderId())
			.publicOrderId(payment.getPublicOrderId())
			.amount(payment.getAmount())
			.currency(payment.getCurrency())
			.status(payment.getStatus())
			.provider(payment.getProvider())
			.paymentUrl(response.url())
			.build();
	}
}