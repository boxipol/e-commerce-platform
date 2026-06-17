package com.pd.ecommerce.event;

import java.util.UUID;

public record OrderItemEvent(
	UUID productId,
	String sku,
	int quantity
) {}