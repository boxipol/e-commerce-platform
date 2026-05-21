package com.pd.ecommerce.service;

import com.pd.ecommerce.dto.CreatePaymentRequest;
import com.pd.ecommerce.dto.PaymentResponse;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;

public interface PaymentService {

	Mono<PaymentResponse> createPayment(CreatePaymentRequest request);
	Mono<ResponseEntity<Void>> handleWebhook(String payload, String signature);
}