package com.pd.ecommerce.events;

import lombok.Builder;
import java.time.Instant;
import java.util.UUID;

@Builder
public record ProductCreatedEvent(
	UUID eventId,
	String eventType,
	Instant occurredAt,
	ProductData product
) {
	@Builder
	public record ProductData(
		UUID id,
		String sku,
		String name,
		String category,
		String brand
	) {}

	public static ProductCreatedEvent of(ProductData product) {
		return new ProductCreatedEvent(
			UUID.randomUUID(),
			"PRODUCT_CREATED",
			Instant.now(),
			product
		);
	}
}