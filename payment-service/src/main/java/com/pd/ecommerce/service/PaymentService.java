package com.pd.ecommerce.service;

import reactor.core.publisher.Mono;

public interface PaymentService {

	Mono<String> getData();
}