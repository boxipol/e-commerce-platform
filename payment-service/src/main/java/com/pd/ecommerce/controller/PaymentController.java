package com.pd.ecommerce.controller;

import com.pd.ecommerce.dto.CreatePaymentRequest;
import com.pd.ecommerce.dto.PaymentResponse;
import com.pd.ecommerce.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public final class PaymentController {

	private final PaymentService paymentService;


	@PostMapping
	public Mono<PaymentResponse> createPayment(@Valid @RequestBody CreatePaymentRequest request){
		return paymentService.createPayment(request);
	}

	@PostMapping("/webhook/stripe")
	public Mono<ResponseEntity<String>> stripeWebhook(
		ServerHttpRequest request,
		@RequestBody String payload,
		@RequestHeader("Stripe-Signature") String signature
	){
		return paymentService.handleWebhook(payload, signature)
			.thenReturn(ResponseEntity.ok("received"));
	}

	@PostMapping("/webhook/paypal")
	public Mono<ResponseEntity<String>> paypalWebhook(
		ServerHttpRequest request,
		@RequestBody String payload,
		@RequestHeader("Stripe-Signature") String signature
	){
		return paymentService.handleWebhook(payload, signature)
			.thenReturn(ResponseEntity.ok("received"));
	}
}