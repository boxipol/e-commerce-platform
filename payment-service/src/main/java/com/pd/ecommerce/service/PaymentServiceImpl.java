package com.pd.ecommerce.service;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public final class PaymentServiceImpl implements PaymentService {

	public Mono<String> getData() {
		return Mono.just("Payment Service is up and running!");
	}
}