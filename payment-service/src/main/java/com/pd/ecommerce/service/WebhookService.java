package com.pd.ecommerce.service;

import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;

public interface WebhookService {

	Mono<ResponseEntity<Void>> handleWebhook(String payload, String signature);
}