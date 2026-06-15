package com.pd.ecommerce.event;

public record OrderEventItem(
	String sku,
	int quantity
) {}