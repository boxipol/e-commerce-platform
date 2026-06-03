package com.pd.ecommerce.service;

import reactor.core.publisher.Mono;
import java.util.Map;

public interface PaypalWebhookService {

	Mono<Void> handle(String payload, Map<String, String> headers);
}