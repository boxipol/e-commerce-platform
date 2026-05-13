package com.pd.ecommerce.event;

import java.math.BigDecimal;
import java.util.List;

public record OrderCreatedEvent(
	String orderId,
	String userId,
	List<String> productIds,
	BigDecimal totalPrice
) {}