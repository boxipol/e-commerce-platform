package com.pd.ecommerce.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record ProductResponse(
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