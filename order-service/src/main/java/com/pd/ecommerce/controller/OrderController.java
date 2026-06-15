package com.pd.ecommerce.controller;

import com.pd.ecommerce.dto.CreateOrderRequest;
import com.pd.ecommerce.dto.OrderResponse;
import com.pd.ecommerce.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public final class OrderController {

	private final OrderService service;


	@GetMapping("/{productId}")
	public Mono<OrderResponse> get(@PathVariable String publicOrderId) {
		return service.getOrder(publicOrderId);
	}

	@PostMapping
	public Mono<OrderResponse> create(
		@RequestHeader("X-User-Id") UUID userId,
		@RequestHeader("X-User-Email") String userMail,
		@RequestBody @Valid CreateOrderRequest request
	) {
		return service.createOrder(userId, userMail, request);
	}
}