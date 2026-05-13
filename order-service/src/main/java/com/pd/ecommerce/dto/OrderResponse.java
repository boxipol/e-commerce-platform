package com.pd.ecommerce.dto;

import lombok.Builder;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Builder
public record OrderResponse(
	UUID id,
	UUID userId,
	String status,
	BigDecimal totalAmount,
	Instant createdAt,
	List<OrderItemResponse> items
) {}