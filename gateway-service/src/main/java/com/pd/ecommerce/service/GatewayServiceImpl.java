package com.pd.ecommerce.service;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public final class GatewayServiceImpl implements GatewayService {

	public Mono<String> getData() {
		return Mono.just("Gateway Service is up and running!");
	}
}