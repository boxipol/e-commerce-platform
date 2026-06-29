package com.pd.ecommerce.event;

import java.util.List;
import java.util.UUID;

public record InventoryReservationCompletedEvent(
	UUID orderId,
	List<OrderEventItem> items
) {}