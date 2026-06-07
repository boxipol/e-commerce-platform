package com.pd.ecommerce.event;

import java.util.UUID;

public record InventoryReservationFailedEvent(
	UUID orderId,
	String reason
) {}