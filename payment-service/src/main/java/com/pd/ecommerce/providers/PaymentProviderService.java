package com.pd.ecommerce.providers;

import com.pd.ecommerce.dto.CreateProviderPaymentRequest;
import com.pd.ecommerce.dto.ProviderPaymentResponse;
import com.pd.ecommerce.entity.PaymentProvider;
import com.pd.ecommerce.entity.PaymentStatus;
import reactor.core.publisher.Mono;

public interface PaymentProviderService {

	Mono<ProviderPaymentResponse> createPayment(CreateProviderPaymentRequest request);
	Mono<Void> refund(String paymentId);
	Mono<PaymentStatus> getStatus(String paymentId);
	PaymentProvider provider();
}