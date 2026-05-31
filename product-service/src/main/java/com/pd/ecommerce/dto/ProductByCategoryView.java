package com.pd.ecommerce.dto;

import lombok.Builder;
import java.math.BigDecimal;
import java.util.UUID;

@Builder
public record ProductByCategoryView(
	UUID id,
	String name,
	String brand,
	BigDecimal price,
	Integer stock
) {}