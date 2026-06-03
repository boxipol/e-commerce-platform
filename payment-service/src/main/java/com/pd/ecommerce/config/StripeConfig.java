package com.pd.ecommerce.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class StripeConfig {

	private final String baseUrl;
	private final String apiKey;


	public StripeConfig(
		@Value("${payment.stripe.base-url}") String baseUrl,
		@Value("${payment.stripe.api-key}") String apiKey
	) {
		this.baseUrl = baseUrl;
		this.apiKey = apiKey;
	}

	@Bean
	public WebClient stripeWebClient() {
		return WebClient.builder()
			.baseUrl(baseUrl)
			.defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
			.build();
	}
}