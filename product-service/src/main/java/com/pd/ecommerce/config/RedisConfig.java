package com.pd.ecommerce.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.pd.ecommerce.dto.ProductPageResponse;
import com.pd.ecommerce.dto.ProductResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

	@Bean
	public ReactiveRedisTemplate<String, ProductResponse> productResponseTemplate(ReactiveRedisConnectionFactory factory, ObjectMapper objectMapper) {
		Jackson2JsonRedisSerializer<ProductResponse> serializer = new Jackson2JsonRedisSerializer<>(objectMapper, ProductResponse.class);

		RedisSerializationContext<String, ProductResponse> context = RedisSerializationContext.<String, ProductResponse> newSerializationContext(new StringRedisSerializer())
			.value(serializer)
			.build();

		return new ReactiveRedisTemplate<>(factory, context);
	}

	@Bean
	public ReactiveRedisTemplate<String, ProductPageResponse> productPageResponseTemplate(ReactiveRedisConnectionFactory factory, ObjectMapper objectMapper) {
		Jackson2JsonRedisSerializer<ProductPageResponse> serializer = new Jackson2JsonRedisSerializer<>(objectMapper, ProductPageResponse.class);
		RedisSerializationContext<String, ProductPageResponse> context = RedisSerializationContext.<String, ProductPageResponse>newSerializationContext(new StringRedisSerializer())
			.value(serializer)
			.build();

		return new ReactiveRedisTemplate<>(factory, context);
	}

	@Bean
	public ObjectMapper objectMapper() {
		return JsonMapper.builder()
			.findAndAddModules()
			.build();
	}
}