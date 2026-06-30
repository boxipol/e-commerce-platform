package com.pd.ecommerce.controller;

import com.pd.ecommerce.dto.CreateOrderRequest;
import com.pd.ecommerce.dto.OrderResponse;
import com.pd.ecommerce.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.UUID;

@Tag(name = "Orders", description = "Order placement and retrieval")
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public final class OrderController {

	private final OrderService service;


	@Operation(summary = "Get order by public ID", description = "Returns order details for the given public order identifier")
	@GetMapping("/{publicOrderId}")
	public Mono<OrderResponse> get(@PathVariable String publicOrderId) {
		return service.getOrder(publicOrderId);
	}

	@Operation(summary = "List my orders", description = "Returns all orders for the authenticated user")
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "Orders returned (empty array if none)"),
		@ApiResponse(responseCode = "401", description = "Missing or invalid JWT")
	})
	@SecurityRequirement(name = "bearerAuth")
	@GetMapping("/my")
	public Flux<OrderResponse> getMyOrders(@Parameter(hidden = true) @RequestHeader("X-User-Id") UUID userId) {
		return service.getOrdersByUser(userId);
	}

	@Operation(summary = "Create order", description = "Places a new order; publishes order.created to Kafka to trigger the payment saga")
	@PostMapping
	public Mono<OrderResponse> create(
		@RequestHeader("X-User-Id") UUID userId,
		@RequestHeader("X-User-Email") String userMail,
		@RequestBody @Valid CreateOrderRequest request
	) {
		return service.createOrder(userId, userMail, request);
	}
}