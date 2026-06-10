package com.pd.ecommerce.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record InventoryCreateRequest(
	@NotNull(message = "Product ID is required")
	UUID productId,

	@NotNull(message = "Quantity is required")
	@Min(value = 0, message = "Quantity must be greater than or equal to 0")
	Integer quantity
) {}