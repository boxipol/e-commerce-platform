package com.pd.ecommerce.event;

import lombok.Builder;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Builder
public record PaymentCompletedEvent(
	UUID paymentId,
	String userMail,
	UUID orderId,
	String publicOrderId,
	List<OrderEventItem> items,
	BigDecimal amount
) {}