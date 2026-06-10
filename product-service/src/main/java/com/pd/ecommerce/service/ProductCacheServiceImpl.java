package com.pd.ecommerce.service;

import com.pd.ecommerce.dto.ProductPageResponse;
import com.pd.ecommerce.dto.ProductResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import java.time.Duration;

@Service
@RequiredArgsConstructor
public class ProductCacheServiceImpl implements ProductCacheService {

	private static final Duration TTL = Duration.ofMinutes(5);

	private final ReactiveRedisTemplate<String, ProductResponse> productResponseTemplate;
	private final ReactiveRedisTemplate<String, ProductPageResponse> productPageResponseTemplate;


	public Mono<ProductResponse> getProduct(String key) {
		return productResponseTemplate.opsForValue()
			.get(key);
	}

	public Mono<ProductResponse> putProduct(String key, ProductResponse response) {
		return productResponseTemplate.opsForValue()
			.set(key, response, TTL)
			.thenReturn(response);
	}

	public Mono<ProductPageResponse> getProducts(String key) {
		return productPageResponseTemplate.opsForValue()
			.get(key);
	}

	public Mono<Boolean> putProducts(String key, ProductPageResponse value) {
		return productPageResponseTemplate.opsForValue()
			.set(key, value, TTL);
	}

	public String key(String category, int pageSize, String cursor) {
		return "products:%s:%d:%s".formatted(category, pageSize, cursor == null ? "null" : cursor);
	}

	public Mono<Void> evictProduct(String key) {
		return productResponseTemplate.delete(key)
			.then();
	}
}