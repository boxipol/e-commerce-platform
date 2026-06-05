package com.pd.ecommerce.event;

import java.util.Map;

public record StripeEvent(
	String id,
	String type,
	Data data
) {
	public record Data(Object object) {}

	public record Object(
		String id,
		String status,
		Long amount,
		String currency,
		Map<String, String> metadata
	) {}
}