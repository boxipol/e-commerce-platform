package com.pd.ecommerce.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record ProductCreateRequest(
	@NotBlank
	String sku,

	@NotBlank
	String name,

	String description,

	@NotBlank
	String brand,

	@NotBlank
	String category,

	@NotNull @DecimalMin("0.0")
	BigDecimal price,

	@NotBlank
	String currency,

	@Min(0)
	Integer stock
) {}