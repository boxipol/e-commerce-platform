package com.pd.ecommerce.providers;

import com.pd.ecommerce.dto.CreatePaymentRequest;
import com.pd.ecommerce.dto.PaymentResponse;
import com.pd.ecommerce.entity.PaymentStatus;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class PaypalPaymentProvider implements PaymentProvider {

	@Override
	public Mono<PaymentResponse> createPayment(CreatePaymentRequest request) {
		return null;
	}

	@Override
	public Mono<Void> refund(String paymentId) {
		return null;
	}

	@Override
	public Mono<PaymentStatus> getStatus(String paymentId) {
		return null;
	}
}