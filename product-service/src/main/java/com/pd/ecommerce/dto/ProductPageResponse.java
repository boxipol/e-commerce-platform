package com.pd.ecommerce.dto;

import java.util.List;

public record ProductPageResponse(
	List<ProductByCategoryView> items,
	String cursor
) {}