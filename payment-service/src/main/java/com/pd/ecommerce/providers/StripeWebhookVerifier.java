package com.pd.ecommerce.providers;

import com.pd.ecommerce.config.StripeProperties;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.net.Webhook;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StripeWebhookVerifier {

	private final StripeProperties properties;


	public Event verify(String payload, String signature) throws SignatureVerificationException {
		return Webhook.constructEvent(payload, signature, properties.webhookSecret());
	}
}