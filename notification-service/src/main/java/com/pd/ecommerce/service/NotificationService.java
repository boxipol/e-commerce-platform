package com.pd.ecommerce.service;

import reactor.core.publisher.Mono;

public interface NotificationService {

	Mono<String> getData();
}