package com.pd.ecommerce.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record ProductUpdateRequest(
	@Size(min = 2, max = 255)
	String name,

	@Size(max = 5000)
	String description,

	@DecimalMin(value = "0.0", inclusive = false)
	BigDecimal price,

	@Min(0) Integer stock
) {}