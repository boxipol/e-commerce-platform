package com.pd.ecommerce.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.math.BigDecimal;
import java.util.UUID;

public record CreatePaymentRequest(
	@NotNull(message = "Order ID is required")
	UUID orderId,

	@NotNull(message = "User ID is required")
	UUID userId,

	@NotNull(message = "Amount is required")
	@DecimalMin(value = "0.01", message = "Amount must be greater than 0")
	BigDecimal amount,

	@NotBlank(message = "Currency is required")
	@Pattern(
		regexp = "^[A-Z]{3}$",
		message = "Currency must be a valid ISO-4217 code"
	)

	String currency
) {}