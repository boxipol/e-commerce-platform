package com.pd.ecommerce.event;

import lombok.Builder;
import java.util.UUID;

@Builder
public record InventoryReservationFailedEvent(
	UUID orderId,
	UUID paymentId,
	String reason
) {}