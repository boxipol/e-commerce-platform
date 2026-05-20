package com.pd.ecommerce.service;

import com.pd.ecommerce.dto.CreatePaymentRequest;
import com.pd.ecommerce.dto.PaymentResponse;
import com.pd.ecommerce.entity.Payment;
import com.pd.ecommerce.entity.PaymentStatus;
import com.pd.ecommerce.kafka.PaymentEventProducer;
import com.pd.ecommerce.mapper.PaymentMapper;
import com.pd.ecommerce.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import java.time.Instant;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

	private final PaymentRepository paymentRepository;
	private final PaymentEventProducer eventProducer;
	private final PaymentMapper paymentMapper;


	public Mono<PaymentResponse> createPayment(CreatePaymentRequest request) {
		Payment payment = paymentMapper.toEntity(request);
		payment.setProvider("STRIPE");
		payment.setStatus(PaymentStatus.PENDING);
		payment.setCreatedAt(Instant.now());
		payment.setUpdatedAt(Instant.now());

		return paymentRepository.save(payment)
			.flatMap(this::processPayment)
			.map(paymentMapper::toResponse);
	}

//	==================== PRIVATE ====================

	private Mono<Payment> processPayment(Payment payment) {
		boolean success = new Random().nextBoolean();

		payment.setStatus(
			success
				? PaymentStatus.COMPLETED
				: PaymentStatus.FAILED
		);

		payment.setUpdatedAt(Instant.now());

		return paymentRepository.save(payment)
			.doOnSuccess(saved -> {
				if (saved.getStatus() == PaymentStatus.COMPLETED) {
					eventProducer.sendPaymentCompleted(saved);
				} else {
					eventProducer.sendPaymentFailed(saved);
				}
			});
	}
}