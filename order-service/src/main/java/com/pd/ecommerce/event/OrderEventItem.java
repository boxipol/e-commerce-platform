package com.pd.ecommerce.event;

import java.util.UUID;

public record OrderEventItem(
	UUID productId,
	String sku,
	int quantity
) {}