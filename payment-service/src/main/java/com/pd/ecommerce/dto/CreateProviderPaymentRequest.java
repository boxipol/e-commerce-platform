package com.pd.ecommerce.dto;

import lombok.Builder;
import java.math.BigDecimal;
import java.util.UUID;

@Builder
public record CreateProviderPaymentRequest(
	UUID paymentId,
	UUID orderId,
	BigDecimal amount,
	String currency
) {}