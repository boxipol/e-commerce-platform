package com.pd.ecommerce.dto;

import lombok.Builder;
import java.time.Instant;
import java.util.UUID;

@Builder
public record InventoryResponse(
	UUID productId,
	Integer quantity,
	Instant createdAt,
	Instant updatedAt
) {}