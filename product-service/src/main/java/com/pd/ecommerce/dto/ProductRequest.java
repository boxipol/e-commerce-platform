package com.pd.ecommerce.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record ProductRequest(
	@NotBlank @Size(min = 3, max = 100)
	String sku,

	@NotBlank @Size(min = 2, max = 255)
	String name,

	@Size(max = 5000)
	String description,

	@NotBlank @Size(min = 2, max = 255)
	String brand,

	@NotBlank @Size(min = 2, max = 100)
	String category,

	@NotNull @DecimalMin(value = "0.0", inclusive = false)
	BigDecimal price,

	@NotBlank @Pattern(regexp = "^[A-Z]{3}$")
	String currency,

	@NotNull @Min(0)
	Integer stock
) {}