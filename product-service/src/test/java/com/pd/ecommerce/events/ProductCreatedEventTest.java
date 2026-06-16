package com.pd.ecommerce.events;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ProductCreatedEvent Tests")
class ProductCreatedEventTest {

	@Test
	@DisplayName("of - should wrap product data with generated metadata")
	void testOf() {
		ProductCreatedEvent.ProductData data = ProductCreatedEvent.ProductData.builder()
			.id(UUID.randomUUID())
			.sku("SKU-1")
			.name("Phone")
			.category("Electronics")
			.brand("Apple")
			.build();

		ProductCreatedEvent event = ProductCreatedEvent.of(data);

		assertThat(event.eventId()).isNotNull();
		assertThat(event.eventType()).isEqualTo("PRODUCT_CREATED");
		assertThat(event.occurredAt()).isNotNull();
		assertThat(event.product()).isSameAs(data);
	}

	@Test
	@DisplayName("of - should generate a unique event id per call")
	void testOfUniqueIds() {
		ProductCreatedEvent.ProductData data = ProductCreatedEvent.ProductData.builder()
			.id(UUID.randomUUID())
			.sku("SKU-1")
			.build();

		assertThat(ProductCreatedEvent.of(data).eventId())
			.isNotEqualTo(ProductCreatedEvent.of(data).eventId());
	}
}