package com.pd.ecommerce.event;

import lombok.Builder;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Builder
public record PaymentCompletedEvent(
	UUID paymentId,
	UUID orderId,
	List<OrderItem> items,
	BigDecimal amount
) {}