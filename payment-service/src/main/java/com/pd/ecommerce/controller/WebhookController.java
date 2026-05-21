package com.pd.ecommerce.controller;

import com.pd.ecommerce.service.WebhookService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/webhooks")
@RequiredArgsConstructor
public final class WebhookController {

	private final WebhookService webhookService;


	@PostMapping("/webhook/stripe")
	public Mono<ResponseEntity<String>> stripeWebhook(
		@RequestBody String payload,
		@RequestHeader("Stripe-Signature") String signature
	){
		return webhookService.handleWebhook(payload, signature)
			.thenReturn(ResponseEntity.ok("received"));
	}

	@PostMapping("/webhook/paypal")
	public Mono<ResponseEntity<String>> paypalWebhook(
		@RequestBody String payload,
		@RequestHeader("Stripe-Signature") String signature
	){
		return webhookService.handleWebhook(payload, signature)
			.thenReturn(ResponseEntity.ok("received"));
	}
}