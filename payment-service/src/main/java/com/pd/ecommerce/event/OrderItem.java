package com.pd.ecommerce.event;

import java.util.UUID;

public record OrderItem(
	UUID productId,
	String sku,
	int quantity
) {}