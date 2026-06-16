package com.pd.ecommerce.service;

import com.pd.ecommerce.dto.ProductByCategoryView;
import com.pd.ecommerce.dto.ProductPageResponse;
import com.pd.ecommerce.dto.ProductResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ReactiveValueOperations;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductCacheServiceImpl")
class ProductCacheServiceImplTest {

	@Mock
	private ReactiveRedisTemplate<String, ProductResponse> productResponseTemplate;

	@Mock
	private ReactiveRedisTemplate<String, ProductPageResponse> productPageResponseTemplate;

	@Mock
	private ReactiveValueOperations<String, ProductResponse> productValueOps;
	@Mock
	private ReactiveValueOperations<String, ProductPageResponse> pageValueOps;
	private ProductCacheServiceImpl cacheService;
	private static final String KEY = "product:BRAND-PROD-ABC123";

	private ProductResponse productResponse;
	private ProductPageResponse pageResponse;

	@BeforeEach
	void setUp() {
		cacheService = new ProductCacheServiceImpl(productResponseTemplate, productPageResponseTemplate);
		productResponse = new ProductResponse(UUID.randomUUID(), "BRAND-PROD-ABC123", "Test Product", "A test product", "Brand", "Electronics", new BigDecimal("99.99"), "USD", true, Instant.now());
		pageResponse = new ProductPageResponse(List.of(ProductByCategoryView.builder().sku("BRAND-PROD-ABC123").name("Test Product").brand("Brand").price(new BigDecimal("99.99")).stock(10).build()), null);
	}
	// ── getProduct ────────────────────────────────────────────────────────────

	@Nested
	@DisplayName("getProduct")
	class GetProduct {

		@Test
		@DisplayName("returns product when key exists in Redis")
		void returnsProductFromRedis() {
			when(productResponseTemplate.opsForValue()).thenReturn(productValueOps);
			when(productValueOps.get(KEY)).thenReturn(Mono.just(productResponse));
			StepVerifier.create(cacheService.getProduct(KEY)).assertNext(r -> assertThat(r.sku()).isEqualTo("BRAND-PROD-ABC123")).verifyComplete();
		}

		@Test
		@DisplayName("returns empty when key does not exist")
		void returnsEmptyWhenMissing() {
			when(productResponseTemplate.opsForValue()).thenReturn(productValueOps);
			when(productValueOps.get(KEY)).thenReturn(Mono.empty());
			StepVerifier.create(cacheService.getProduct(KEY)).verifyComplete();
		}
	}
	// ── putProduct ────────────────────────────────────────────────────────────

	@Nested
	@DisplayName("putProduct")
	class PutProduct {

		@Test
		@DisplayName("stores product with 5-minute TTL and returns it")
		void storesProductWithTtl() {
			when(productResponseTemplate.opsForValue()).thenReturn(productValueOps);
			when(productValueOps.set(eq(KEY), eq(productResponse), eq(Duration.ofMinutes(5)))).thenReturn(Mono.just(true));
			StepVerifier.create(cacheService.putProduct(KEY, productResponse)).assertNext(r -> assertThat(r).isEqualTo(productResponse)).verifyComplete();
			verify(productValueOps).set(KEY, productResponse, Duration.ofMinutes(5));
		}
	}
	// ── getProducts ───────────────────────────────────────────────────────────

	@Nested
	@DisplayName("getProducts")
	class GetProducts {

		@Test
		@DisplayName("returns page response when key exists")
		void returnsPageFromRedis() {
			when(productPageResponseTemplate.opsForValue()).thenReturn(pageValueOps);
			when(pageValueOps.get(KEY)).thenReturn(Mono.just(pageResponse));
			StepVerifier.create(cacheService.getProducts(KEY)).assertNext(r -> assertThat(r.items()).hasSize(1)).verifyComplete();
		}

		@Test
		@DisplayName("returns empty when key does not exist")
		void returnsEmptyWhenMissing() {
			when(productPageResponseTemplate.opsForValue()).thenReturn(pageValueOps);
			when(pageValueOps.get(KEY)).thenReturn(Mono.empty());
			StepVerifier.create(cacheService.getProducts(KEY)).verifyComplete();
		}
	}
	// ── putProducts ───────────────────────────────────────────────────────────

	@Nested
	@DisplayName("putProducts")
	class PutProducts {
		@Test
		@DisplayName("stores page response with 5-minute TTL and returns true")
		void storesPageWithTtl() {
			when(productPageResponseTemplate.opsForValue()).thenReturn(pageValueOps);
			when(pageValueOps.set(eq(KEY), eq(pageResponse), eq(Duration.ofMinutes(5)))).thenReturn(Mono.just(true));
			StepVerifier.create(cacheService.putProducts(KEY, pageResponse)).assertNext(result -> assertThat(result).isTrue()).verifyComplete();
		}
	}
	// ── evictProduct ──────────────────────────────────────────────────────────

	@Nested
	@DisplayName("evictProduct")
	class EvictProduct {
		@Test
		@DisplayName("deletes key from Redis and completes")
		void deletesKeyFromRedis() {
			when(productResponseTemplate.delete(KEY)).thenReturn(Mono.just(1L));
			StepVerifier.create(cacheService.evictProduct(KEY)).verifyComplete();
			verify(productResponseTemplate).delete(KEY);
		}

		@Test
		@DisplayName("completes even when key does not exist (delete returns 0)")
		void completesWhenKeyAbsent() {
			when(productResponseTemplate.delete(KEY)).thenReturn(Mono.just(0L));
			StepVerifier.create(cacheService.evictProduct(KEY)).verifyComplete();
		}
	}
	// ── key ───────────────────────────────────────────────────────────────────

	@Nested
	@DisplayName("key")
	class KeyGeneration {
		@Test
		@DisplayName("generates key with category, pageSize, and cursor")
		void generatesKeyWithCursor() {
			String key = cacheService.key("Electronics", 20, "abc123");
			assertThat(key).isEqualTo("products:Electronics:20:abc123");
		}

		@Test
		@DisplayName("uses 'null' literal when cursor is null")
		void generatesKeyWithNullCursor() {
			String key = cacheService.key("Electronics", 20, null);
			assertThat(key).isEqualTo("products:Electronics:20:null");
		}

		@Test
		@DisplayName("includes pageSize in key to differentiate page sizes")
		void includesPageSizeInKey() {
			String key10 = cacheService.key("Electronics", 10, null);
			String key50 = cacheService.key("Electronics", 50, null);
			assertThat(key10).isNotEqualTo(key50);
		}
	}
}