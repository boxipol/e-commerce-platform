package com.pd.ecommerce.dto;

import com.pd.ecommerce.entity.OrderStatus;
import lombok.Builder;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Builder
public record OrderResponse(
	String publicOrderId,
	String userMail,
	OrderStatus status,
	BigDecimal totalAmount,
	Instant createdAt,
	List<OrderItemResponse> items
) {}