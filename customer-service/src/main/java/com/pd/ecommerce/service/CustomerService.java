package com.pd.ecommerce.service;

import reactor.core.publisher.Mono;

public interface CustomerService {

	Mono<String> getData();
}