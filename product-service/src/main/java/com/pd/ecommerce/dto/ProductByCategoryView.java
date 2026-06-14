package com.pd.ecommerce.dto;

import lombok.Builder;
import java.math.BigDecimal;

@Builder
public record ProductByCategoryView(
	String sku,
	String name,
	String brand,
	BigDecimal price,
	Integer stock
) {}