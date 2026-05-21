package com.pd.ecommerce.event;

import lombok.Builder;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Builder
public record OrderCreatedEvent(
	UUID orderId,
	UUID userId,
	List<String> productIds,
	BigDecimal totalPrice
) {}