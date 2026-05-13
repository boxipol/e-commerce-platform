package com.pd.ecommerce.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderItemResponse(
	UUID productId,
	Integer quantity,
	BigDecimal unitPrice,
	BigDecimal subtotal
) {}