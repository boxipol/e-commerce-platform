package com.pd.ecommerce.dto;

public record ProductResponse(
	Long productId,
	ProductStatus status
) {}