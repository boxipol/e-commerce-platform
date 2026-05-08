package com.pd.ecommerce.service;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public final class NotificationServiceImpl implements NotificationService {

	public Mono<String> getData() {
		return Mono.just("Notification Service is up and running!");
	}
}