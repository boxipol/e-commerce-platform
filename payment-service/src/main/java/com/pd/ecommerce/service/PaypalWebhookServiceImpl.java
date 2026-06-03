package com.pd.ecommerce.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import java.util.Map;

@Service
@RequiredArgsConstructor
final class PaypalWebhookServiceImpl implements PaypalWebhookService {

	private final PaymentService paymentService;

	@Override
	public Mono<Void> handle(String payload, Map<String, String> headers) {
		return null;
	}
}
