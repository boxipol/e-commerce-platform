package com.pd.ecommerce.providers;

import com.pd.ecommerce.dto.CreatePaymentRequest;
import com.pd.ecommerce.dto.PaymentResponse;
import com.pd.ecommerce.entity.PaymentStatus;
import reactor.core.publisher.Mono;

public interface PaymentProvider {

	Mono<PaymentResponse> createPayment(CreatePaymentRequest request);
	Mono<Void> refund(String paymentId);
	Mono<PaymentStatus> getStatus(String paymentId);
}