package com.pd.ecommerce.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record OrderRequest(

	@NotNull(message = "Product ID cannot be null")
	Long userId,

	@Min(value = 1, message = "Quantity must be at least 1")
	List<OrderItemRequest> items
) {}