package com.pd.ecommerce.event;

import lombok.Builder;
import java.time.Instant;

@Builder
public record UserDeletedEvent(
	String email,
	Instant createdAt
) {}