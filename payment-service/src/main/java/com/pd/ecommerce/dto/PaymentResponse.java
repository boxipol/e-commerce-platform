package com.pd.ecommerce.dto;

import com.pd.ecommerce.entity.PaymentProvider;
import com.pd.ecommerce.entity.PaymentStatus;
import lombok.Builder;
import java.math.BigDecimal;
import java.util.UUID;

@Builder
public record PaymentResponse(
	UUID id,
	UUID orderId,
	String publicOrderId,
	BigDecimal amount,
	String currency,
	PaymentStatus status,
	PaymentProvider provider,
	String paymentUrl
) {}