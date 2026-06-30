package com.pd.ecommerce.service;

import com.pd.ecommerce.dto.CreateOrderRequest;
import com.pd.ecommerce.dto.OrderResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.UUID;

public interface OrderService {

	Mono<OrderResponse> getOrder(String publicOrderId);
	Flux<OrderResponse> getOrdersByUser(UUID userId);
	Mono<OrderResponse> createOrder(UUID userID, String userMail, CreateOrderRequest request);
	Mono<Void> markAsPaid(UUID orderId);
	Mono<Void> markAsFailed(UUID orderId);
}