package com.pd.ecommerce.event;

import lombok.Builder;
import java.util.UUID;

@Builder
public record InventoryReservationFailedEvent(
	UUID orderId,
	String publicOrderId,
	UUID paymentId,
	String userMail,
	String reason
) {}