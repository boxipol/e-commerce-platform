package com.pd.ecommerce.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "payment.stripe")
public record StripeProperties(
	String baseUrl,
	String apiKey,
	String webhookSecret
) {}