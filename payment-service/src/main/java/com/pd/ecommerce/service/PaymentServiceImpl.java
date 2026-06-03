package com.pd.ecommerce.service;

import com.pd.ecommerce.dto.CreateProviderPaymentRequest;
import com.pd.ecommerce.dto.PaymentResponse;
import com.pd.ecommerce.dto.ProviderPaymentResponse;
import com.pd.ecommerce.entity.Payment;
import com.pd.ecommerce.entity.PaymentProvider;
import com.pd.ecommerce.entity.PaymentStatus;
import com.pd.ecommerce.event.OrderCreatedEvent;
import com.pd.ecommerce.kafka.PaymentEventProducer;
import com.pd.ecommerce.providers.PaymentProviderRegistry;
import com.pd.ecommerce.providers.PaymentProviderService;
import com.pd.ecommerce.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import java.time.Instant;
import java.util.Random;

@Service
@RequiredArgsConstructor
public final class PaymentServiceImpl implements PaymentService {

	private final PaymentRepository repository;
	private final PaymentEventProducer eventProducer;
	private final PaymentProviderRegistry paymentProviderRegistry;


	public Mono<PaymentResponse> createPayment(OrderCreatedEvent event) {
		PaymentProviderService paymentService = paymentProviderRegistry.get(resolveProvider());
		Instant createdAt = Instant.now();

		Payment payment = Payment.builder()
			.orderId(event.orderId())
			.userId(event.userId())
			.amount(event.totalPrice())
			.currency("EUR") // todo add ccy provider
			.status(PaymentStatus.PENDING)
			.provider(paymentService.provider())
			.createdAt(createdAt)
			.updatedAt(createdAt)
			.build();

		return repository.save(payment)
			.flatMap(saved ->
				paymentService.createPayment(toProviderRequest(payment))
					.map(response -> updatePayment(saved, response))
					.flatMap(repository::save)
			).map(this::toResponse);
	}

//	==================== PRIVATE ====================

	// todo resolve based on some logic
	private PaymentProvider resolveProvider() {
		boolean success = new Random().nextBoolean();

		return success
			? PaymentProvider.STRIPE
			: PaymentProvider.PAYPAL;
	}

	private CreateProviderPaymentRequest toProviderRequest(Payment payment) {
		return CreateProviderPaymentRequest.builder()
			.paymentId(payment.getId())
			.orderId(payment.getOrderId())
			.amount(payment.getAmount())
			.currency(payment.getCurrency())
			.build();
	}

	private Payment updatePayment(Payment payment, ProviderPaymentResponse response) {
		payment.setProviderPaymentId(response.id());
		payment.setPaymentUrl(response.url());
		payment.setUpdatedAt(Instant.now());

		return payment;
	}

	private PaymentResponse toResponse(Payment payment) {
		return PaymentResponse.builder()
			.id(payment.getId())
			.orderId(payment.getOrderId())
			.amount(payment.getAmount())
			.currency(payment.getCurrency())
			.status(payment.getStatus())
			.provider(payment.getProvider())
			.paymentUrl(payment.getPaymentUrl())
			.build();
	}
}