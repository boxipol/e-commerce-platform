package com.pd.ecommerce.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@RequiredArgsConstructor
public class PayPalConfig {

	@Bean
	public WebClient payPalWebClient(@Value("${payment.paypal.base-url}") String baseUrl) {

		return WebClient.builder()
			.baseUrl(baseUrl)
			.defaultHeader(
				HttpHeaders.CONTENT_TYPE,
				MediaType.APPLICATION_JSON_VALUE
			)
			.build();
	}
}