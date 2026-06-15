package com.pd.ecommerce.event;

public record OrderItemEvent(
	String sku,
	int quantity
) {}