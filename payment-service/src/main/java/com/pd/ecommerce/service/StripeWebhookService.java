package com.pd.ecommerce.service;

import reactor.core.publisher.Mono;

public interface StripeWebhookService {

	Mono<Void> handle(String payload, String signature);
}