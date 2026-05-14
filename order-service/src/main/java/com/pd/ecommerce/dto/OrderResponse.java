package com.pd.ecommerce.dto;

import com.pd.ecommerce.entity.OrderStatus;
import lombok.Builder;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Builder
public record OrderResponse(
	UUID id,
	UUID userId,
	OrderStatus status,
	BigDecimal totalAmount,
	Instant createdAt,
	List<OrderItemResponse> items
) {}