package com.pd.ecommerce.client;

import com.pd.ecommerce.dto.CreatePaymentRequest;
import com.pd.ecommerce.dto.PaymentResponse;
import reactor.core.publisher.Mono;

public interface PaymentServiceClient {

	Mono<PaymentResponse> createPayment(CreatePaymentRequest request);
}