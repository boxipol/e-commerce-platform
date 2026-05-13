package com.pd.ecommerce.service;

import com.pd.ecommerce.dto.CreateOrderRequest;
import com.pd.ecommerce.dto.OrderResponse;
import reactor.core.publisher.Mono;
import java.util.UUID;

public interface OrderService {

	Mono<OrderResponse> getOrder(UUID id);
	Mono<OrderResponse> createOrder(CreateOrderRequest request);
}