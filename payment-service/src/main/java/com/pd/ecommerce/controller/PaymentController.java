package com.pd.ecommerce.controller;

import com.pd.ecommerce.dto.PaymentResponse;
import com.pd.ecommerce.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import java.util.UUID;

@Tag(name = "Payments", description = "Payment record queries")
@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public final class PaymentController {

	private final PaymentService service;


	@Operation(summary = "Get payment by ID", description = "Returns payment details for the given payment ID")
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "Payment found"),
		@ApiResponse(responseCode = "401", description = "Missing or invalid JWT"),
		@ApiResponse(responseCode = "404", description = "Payment not found")
	})
	@SecurityRequirement(name = "bearerAuth")
	@GetMapping("/{id}")
	public Mono<PaymentResponse> getById(
		@Parameter(description = "Payment UUID", required = true) @PathVariable UUID id
	) {
		return service.getById(id);
	}

	@Operation(summary = "Get payment by order ID", description = "Returns the payment associated with the given internal order ID")
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "Payment found"),
		@ApiResponse(responseCode = "401", description = "Missing or invalid JWT"),
		@ApiResponse(responseCode = "404", description = "No payment found for this order")
	})
	@SecurityRequirement(name = "bearerAuth")
	@GetMapping
	public Mono<PaymentResponse> getByOrderId(
		@Parameter(description = "Internal order UUID (from order-service)", required = true) @RequestParam UUID orderId
	) {
		return service.getByOrderId(orderId);
	}
}
