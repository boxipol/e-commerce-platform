package com.pd.ecommerce.controller;

import com.pd.ecommerce.service.PaypalWebhookService;
import com.pd.ecommerce.service.StripeWebhookService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/webhooks")
@RequiredArgsConstructor
public final class WebhookController {

	private final StripeWebhookService stripeWebhookService;
	private final PaypalWebhookService paypalWebhookService;


	@PostMapping("/stripe")
	public Mono<Void> stripeWebhook(
		@RequestBody String payload,
		@RequestHeader("Stripe-Signature") String signature
	) {
		return stripeWebhookService.handle(payload, signature);
	}

	@PostMapping("/paypal")
	public Mono<Void> paypalWebhook(
		@RequestBody String payload,
		@RequestHeader Map<String, String> headers
	) {
		return paypalWebhookService.handle(payload, headers);
	}
}