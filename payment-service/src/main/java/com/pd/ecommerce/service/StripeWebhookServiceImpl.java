package com.pd.ecommerce.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
final class StripeWebhookServiceImpl implements StripeWebhookService {

	private final PaymentService paymentService;

	@Override
	public Mono<Void> handle(String payload, String signature) {
		return null;
	}
}
