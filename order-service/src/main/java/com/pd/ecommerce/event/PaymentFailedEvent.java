package com.pd.ecommerce.event;

import lombok.Builder;
import java.math.BigDecimal;
import java.util.UUID;

@Builder
public record PaymentFailedEvent(
	UUID paymentId,
	String userMail,
	UUID orderId,
	String publicOrderId,
	BigDecimal amount
) {}