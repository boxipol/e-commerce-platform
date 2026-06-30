package com.pd.ecommerce.controller;

import com.pd.ecommerce.service.PaypalWebhookService;
import com.pd.ecommerce.service.StripeWebhookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import java.util.Map;

@Tag(name = "Payment Webhooks", description = "Inbound webhook callbacks from Stripe and PayPal (JWT-exempt, signature-verified)")
@RestController
@RequestMapping("/api/v1/payments/webhooks")
@RequiredArgsConstructor
public final class WebhookController {

	private final StripeWebhookService stripeWebhookService;
	private final PaypalWebhookService paypalWebhookService;


	@Operation(summary = "Stripe webhook", description = "Receives Stripe event payloads; validates Stripe-Signature header before processing")
	@PostMapping("/stripe")
	public Mono<Void> stripeWebhook(
		@RequestBody String payload,
		@RequestHeader("Stripe-Signature") String signature
	) {
		return stripeWebhookService.handle(payload, signature);
	}

	@Operation(summary = "PayPal webhook", description = "Receives PayPal event payloads; validates headers before processing")
	@PostMapping("/paypal")
	public Mono<Void> paypalWebhook(
		@RequestBody String payload,
		@RequestHeader Map<String, String> headers
	) {
		return paypalWebhookService.handle(payload, headers);
	}
}