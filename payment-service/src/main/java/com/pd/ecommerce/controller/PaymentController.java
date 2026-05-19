package com.pd.ecommerce.controller;

import com.pd.ecommerce.dto.CreatePaymentRequest;
import com.pd.ecommerce.dto.PaymentResponse;
import com.pd.ecommerce.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public final class PaymentController {

	private final PaymentService paymentService;


	@PostMapping
	public Mono<PaymentResponse> createPayment(@RequestBody CreatePaymentRequest request){
		return paymentService.createPayment(request);
	}
}