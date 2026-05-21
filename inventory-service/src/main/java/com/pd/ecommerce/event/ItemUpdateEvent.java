package com.pd.ecommerce.event;

import lombok.Builder;
import java.util.UUID;

@Builder
public record ItemUpdateEvent(
	UUID id
) {}