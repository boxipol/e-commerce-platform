package com.pd.ecommerce.config;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.pd.ecommerce.dto.ProductByCategoryView;
import com.pd.ecommerce.dto.ProductResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import java.util.List;

@Configuration
public class RedisConfig {

	@Bean
	public ReactiveRedisTemplate<String, ProductResponse> productRedisTemplate(ReactiveRedisConnectionFactory factory, ObjectMapper objectMapper) {
		Jackson2JsonRedisSerializer<ProductResponse> serializer = new Jackson2JsonRedisSerializer<>(objectMapper, ProductResponse.class);

		RedisSerializationContext<String, ProductResponse> context = RedisSerializationContext
				.<String, ProductResponse>newSerializationContext(new StringRedisSerializer())
				.value(serializer)
				.build();

		return new ReactiveRedisTemplate<>(factory, context);
	}

	@Bean
	public ReactiveRedisTemplate<String, List<ProductByCategoryView>> categoryRedisTemplate(ReactiveRedisConnectionFactory factory, ObjectMapper objectMapper) {
		JavaType type = objectMapper.getTypeFactory()
			.constructCollectionType(List.class, ProductByCategoryView.class);

		Jackson2JsonRedisSerializer<List<ProductByCategoryView>> serializer = new Jackson2JsonRedisSerializer<>(objectMapper, type);

		RedisSerializationContext<String, List<ProductByCategoryView>> context = RedisSerializationContext
				.<String, List<ProductByCategoryView>>newSerializationContext(new StringRedisSerializer())
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