package org.bbb.dto;

import java.time.Instant;

public record OrderResponse(

	Long orderId,
	String status,
	Instant createdAt
) {}