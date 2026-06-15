package com.pd.ecommerce.event;

import lombok.Builder;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Builder
public record OrderCreatedEvent(
	UUID orderId,
	String publicOrderId,
	UUID userId,
	String userMail,
	List<OrderEventItem> items,
	BigDecimal totalPrice
) {}