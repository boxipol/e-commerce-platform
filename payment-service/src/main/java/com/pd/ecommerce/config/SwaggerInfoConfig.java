package com.pd.ecommerce.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerInfoConfig {

	@Bean
	public OpenAPI baseOpenAPI() {
		return new OpenAPI()
			.info(new Info()
				.title("SmartShop Payment API")
				.version("1.0")
				.description("Payment management service with versioned APIs"));
	}
}