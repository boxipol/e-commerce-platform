package com.bbb.config;

import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.OpenAPI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerInfoConfig {

	@Bean
	public OpenAPI baseOpenAPI() {
		return new OpenAPI()
			.info(new Info()
				.title("SmartShop Order API")
				.version("1.0")
				.description("Order management service with versioned APIs"));
	}
}