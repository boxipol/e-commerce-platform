package com.pd.ecommerce.service;

import reactor.core.publisher.Mono;


public interface OrderService {

	Mono<String> getData();
//	OrderResponse getOrder(Long orderId);
//	OrderResponse placeOrder(OrderRequest request);
}