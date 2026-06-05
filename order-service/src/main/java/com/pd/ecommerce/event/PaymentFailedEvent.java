package com.pd.ecommerce.event;

import java.math.BigDecimal;
import java.util.UUID;

public record PaymentFailedEvent(
	UUID paymentId,
	UUID orderId,
	BigDecimal amount
) {}