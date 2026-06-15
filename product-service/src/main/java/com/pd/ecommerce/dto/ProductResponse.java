package com.pd.ecommerce.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ProductResponse(
	UUID productId,
	String sku,
	String name,
	String description,
	String brand,
	String category,
	BigDecimal price,
	String currency,
	Boolean available, // derived from stock/active
	Instant createdAt
) {}