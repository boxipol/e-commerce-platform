package com.pd.ecommerce.config;

import feign.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class FeignConfig {

	@Bean
	public Logger.Level feignLoggerLevel() {
		return Logger.Level.FULL;
	}

//	@Bean
//	public Request.Options options() {
//		return new Request.Options(
//			3000,  // connect timeout
//			5000   // read timeout
//		);
//	}
}