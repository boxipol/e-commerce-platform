package com.pd.ecommerce.service;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public final class CustomerServiceImpl implements CustomerService {

	@Override
	public Mono<String> getData() {
		return Mono.just("Customer Service is up and running!");
	}
}