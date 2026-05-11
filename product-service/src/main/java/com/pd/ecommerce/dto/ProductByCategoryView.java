package com.pd.ecommerce.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductByCategoryView(
	UUID id,
	String name,
	String brand,
	BigDecimal price,
	Integer stock
) {}