package com.pd.ecommerce.service;

import reactor.core.publisher.Mono;

public interface GatewayService {

	Mono<String> getData();
}