package com.pd.ecommerce.service;

import com.pd.ecommerce.dto.ProductByCategoryView;
import com.pd.ecommerce.dto.ProductCreateRequest;
import com.pd.ecommerce.dto.ProductPageResponse;
import com.pd.ecommerce.dto.ProductResponse;
import com.pd.ecommerce.dto.ProductUpdateRequest;
import com.pd.ecommerce.entity.Product;
import com.pd.ecommerce.entity.ProductByCategory;
import com.pd.ecommerce.entity.ProductByCategoryKey;
import com.pd.ecommerce.entity.ProductBySku;
import com.pd.ecommerce.mapper.ProductMapper;
import com.pd.ecommerce.repository.ProductByCategoryQueryRepository;
import com.pd.ecommerce.repository.ProductByCategoryRepository;
import com.pd.ecommerce.repository.ProductBySkuRepository;
import com.pd.ecommerce.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductServiceImpl")
class ProductServiceImplTest {

	@Mock
	private ProductRepository productRepository;

	@Mock
	private ProductBySkuRepository productBySkuRepository;

	@Mock
	private ProductByCategoryRepository productByCategoryRepository;

	@Mock
	private ProductByCategoryQueryRepository productByCategoryQueryRepository;

	@Mock
	private ProductMapper productMapper;

	@Mock
	private ProductCacheService cacheService;

	@InjectMocks
	private ProductServiceImpl productService;

	// ── shared fixtures ──────────────────────────────────────────────────────
	private static final String SKU = "BRAND-PROD-ABC123";
	private static final UUID PRODUCT_ID = UUID.randomUUID();
	private static final String CACHE_KEY = "product:" + SKU;

	private Product product;
	private ProductBySku productBySku;
	private ProductResponse productResponse;


	@BeforeEach
	void setUp() {
		product = new Product();
		product.setProductId(PRODUCT_ID);
		product.setSku(SKU);
		product.setName("Test Product");
		product.setDescription("A test product");
		product.setBrand("Brand");
		product.setCategory("Electronics");
		product.setPrice(new BigDecimal("99.99"));
		product.setCurrency("USD");
		product.setStock(10);
		product.setActive(true);
		product.setCreatedAt(Instant.now());
		product.setUpdatedAt(Instant.now());
		productBySku = new ProductBySku();
		productBySku.setSku(SKU);
		productBySku.setProductId(PRODUCT_ID);
		productBySku.setName("Test Product");
		productBySku.setStock(10);
		productBySku.setActive(true);
		productResponse = new ProductResponse(PRODUCT_ID, SKU, "Test Product", "A test product", "Brand", "Electronics", new BigDecimal("99.99"), "USD", true, Instant.now());
	}
	// ── getBySku ─────────────────────────────────────────────────────────────

	@Nested
	@DisplayName("getBySku")
	class GetBySku {

		@Test
		@DisplayName("returns cached product on cache hit")
		void returnsCachedProduct() {
			when(cacheService.getProduct(CACHE_KEY)).thenReturn(Mono.just(productResponse));
			StepVerifier.create(productService.getBySku(SKU)).assertNext(r -> assertThat(r.sku()).isEqualTo(SKU)).verifyComplete();
			verify(productBySkuRepository, never()).findById(anyString());
		}

		@Test
		@DisplayName("fetches from Cassandra and populates cache on cache miss")
		void fetchesFromCassandraOnCacheMiss() {
			when(cacheService.getProduct(CACHE_KEY)).thenReturn(Mono.empty());
			when(productBySkuRepository.findById(SKU)).thenReturn(Mono.just(productBySku));
			when(productMapper.toResponse(productBySku)).thenReturn(productResponse);
			when(cacheService.putProduct(CACHE_KEY, productResponse)).thenReturn(Mono.just(productResponse));
			StepVerifier.create(productService.getBySku(SKU)).assertNext(r -> assertThat(r.sku()).isEqualTo(SKU)).verifyComplete();
			verify(productBySkuRepository).findById(SKU);
			verify(cacheService).putProduct(CACHE_KEY, productResponse);
		}

		@Test
		@DisplayName("returns 404 when product not found in cache or Cassandra")
		void returns404WhenNotFound() {
			when(cacheService.getProduct(CACHE_KEY)).thenReturn(Mono.empty());
			when(productBySkuRepository.findById(SKU)).thenReturn(Mono.empty());
			StepVerifier.create(productService.getBySku(SKU))
				.expectErrorMatches(ex -> ex instanceof org.springframework.web.server.ResponseStatusException rse
					&& rse.getStatusCode() == org.springframework.http.HttpStatus.NOT_FOUND
					&& rse.getReason().contains(SKU))
				.verify();
		}
	}
	// ── getProducts ───────────────────────────────────────────────────────────

	@Nested
	@DisplayName("getProducts")
	class GetProducts {

		@Test
		@DisplayName("returns all products for given SKU list")
		void returnsAllProducts() {
			String sku2 = "BRAND-PROD-XYZ789";
			ProductResponse response2 = new ProductResponse(UUID.randomUUID(), sku2, "Product 2", null, "Brand", "Electronics", new BigDecimal("49.99"), "USD", true, Instant.now());
			when(cacheService.getProduct("product:" + SKU)).thenReturn(Mono.just(productResponse));
			when(cacheService.getProduct("product:" + sku2)).thenReturn(Mono.just(response2));
			StepVerifier.create(productService.getProducts(List.of(SKU, sku2))).expectNextCount(2).verifyComplete();
		}

		@Test
		@DisplayName("returns empty flux for empty SKU list")
		void returnsEmptyFluxForEmptyList() {
			StepVerifier.create(productService.getProducts(List.of())).verifyComplete();
		}
	}
	// ── getByCategory ─────────────────────────────────────────────────────────

	@Nested
	@DisplayName("getByCategory")
	class GetByCategory {

		private static final String CATEGORY = "Electronics";
		private static final String CACHE_KEY_CAT = "products:Electronics:20:null";

		@Test
		@DisplayName("returns cached page on cache hit")
		void returnsCachedPage() {
			ProductPageResponse page = new ProductPageResponse(List.of(), null);
			when(cacheService.key(CATEGORY, 20, null)).thenReturn(CACHE_KEY_CAT);
			when(cacheService.getProducts(CACHE_KEY_CAT)).thenReturn(Mono.just(page));
			StepVerifier.create(productService.getByCategory(CATEGORY, 20, null)).assertNext(r -> assertThat(r.items()).isEmpty()).verifyComplete();
			verify(productByCategoryQueryRepository, never()).findByCategory(any(), any(int.class), any());
		}

		@Test
		@DisplayName("fetches from Cassandra and caches result on cache miss")
		void fetchesFromCassandraOnCacheMiss() {
			ProductByCategoryView view = ProductByCategoryView.builder().sku(SKU).name("Test Product").brand("Brand").price(new BigDecimal("99.99")).stock(10).build();
			ProductPageResponse page = new ProductPageResponse(List.of(view), null);
			when(cacheService.key(CATEGORY, 20, null)).thenReturn(CACHE_KEY_CAT);
			when(cacheService.getProducts(CACHE_KEY_CAT)).thenReturn(Mono.empty());
			when(productByCategoryQueryRepository.findByCategory(CATEGORY, 20, null)).thenReturn(Mono.just(page));
			when(cacheService.putProducts(CACHE_KEY_CAT, page)).thenReturn(Mono.just(true));
			StepVerifier.create(productService.getByCategory(CATEGORY, 20, null)).assertNext(r -> {
				assertThat(r.items()).hasSize(1);
				assertThat(r.items().get(0).sku()).isEqualTo(SKU);
			}).verifyComplete();
			verify(cacheService).putProducts(CACHE_KEY_CAT, page);
		}

		@Test
		@DisplayName("passes pageState cursor to repository")
		void passesCursorToRepository() {
			String cursor = "dGVzdC1jdXJzb3I=";
			String cursorKey = "products:Electronics:20:" + cursor;
			ProductPageResponse page = new ProductPageResponse(List.of(), "nextCursor");
			when(cacheService.key(CATEGORY, 20, cursor)).thenReturn(cursorKey);
			when(cacheService.getProducts(cursorKey)).thenReturn(Mono.empty());
			when(productByCategoryQueryRepository.findByCategory(CATEGORY, 20, cursor)).thenReturn(Mono.just(page));
			when(cacheService.putProducts(cursorKey, page)).thenReturn(Mono.just(true));
			StepVerifier.create(productService.getByCategory(CATEGORY, 20, cursor)).assertNext(r -> assertThat(r.cursor()).isEqualTo("nextCursor")).verifyComplete();
		}
	}
	// ── create ────────────────────────────────────────────────────────────────

	@Nested
	@DisplayName("create")
	class Create {

		private ProductCreateRequest createRequest;


		@BeforeEach
		void setUp() {
			createRequest = new ProductCreateRequest(SKU, "Test Product", "A test product", "Brand", "Electronics", new BigDecimal("99.99"), "USD", 10);
		}

		@Test
		@DisplayName("saves product to all three tables and returns response")
		void savesProductAndReturnsResponse() {
			ProductByCategory categoryProjection = ProductByCategory.builder().key(ProductByCategoryKey.builder().category("Electronics").createdAt(Instant.now()).sku(SKU).build()).name("Test Product").brand("Brand").price(new BigDecimal("99.99")).stock(10).build();
			when(productMapper.toProduct(createRequest)).thenReturn(product);
			when(productRepository.save(product)).thenReturn(Mono.just(product));
			when(productMapper.toProductBySku(createRequest)).thenReturn(productBySku);
			when(productBySkuRepository.save(productBySku)).thenReturn(Mono.just(productBySku));
			when(productMapper.toProductByCategoryView(product)).thenReturn(categoryProjection);
			when(productByCategoryRepository.save(categoryProjection)).thenReturn(Mono.just(categoryProjection));
			when(productMapper.toResponse(product)).thenReturn(productResponse);
			StepVerifier.create(productService.create(createRequest)).assertNext(r -> {
				assertThat(r.sku()).isEqualTo(SKU);
				assertThat(r.name()).isEqualTo("Test Product");
			}).verifyComplete();
			verify(productRepository).save(product);
			verify(productBySkuRepository).save(productBySku);
			verify(productByCategoryRepository).save(categoryProjection);
		}

		@Test
		@DisplayName("propagates error when main product save fails")
		void propagatesErrorOnSaveFailure() {
			when(productMapper.toProduct(createRequest)).thenReturn(product);
			when(productRepository.save(product)).thenReturn(Mono.error(new RuntimeException("Cassandra unavailable")));
			StepVerifier.create(productService.create(createRequest)).expectErrorMessage("Cassandra unavailable").verify();
		}
	}
	// ── update ────────────────────────────────────────────────────────────────

	@Nested
	@DisplayName("update")
	class Update {

		private ProductUpdateRequest updateRequest;


		@BeforeEach
		void setUp() {
			updateRequest = new ProductUpdateRequest("Updated Name", "Updated description", new BigDecimal("149.99"), 5);
		}

		@Test
		@DisplayName("updates product fields and evicts cache")
		void updatesProductAndEvictsCache() {
			ProductByCategory projection = ProductByCategory.builder().key(ProductByCategoryKey.builder().category("Electronics").createdAt(product.getCreatedAt()).sku(SKU).build()).name("Updated Name").brand("Brand").price(new BigDecimal("149.99")).stock(5).build();
			when(productRepository.findById(PRODUCT_ID)).thenReturn(Mono.just(product));
			when(productMapper.toProductByCategoryView(any(Product.class))).thenReturn(projection);
			when(productRepository.save(any(Product.class))).thenReturn(Mono.just(product));
			when(productByCategoryRepository.save(projection)).thenReturn(Mono.just(projection));
			when(cacheService.evictProduct(CACHE_KEY)).thenReturn(Mono.empty());
			when(productMapper.toResponse(product)).thenReturn(productResponse);
			StepVerifier.create(productService.update(PRODUCT_ID, updateRequest)).assertNext(r -> assertThat(r.sku()).isEqualTo(SKU)).verifyComplete();
			verify(cacheService).evictProduct(CACHE_KEY);
		}

		@Test
		@DisplayName("deletes old category projection and saves new one when category changes")
		void handlesCategoryChange() {
			Product updatedProduct = new Product();
			updatedProduct.setProductId(PRODUCT_ID);
			updatedProduct.setSku(SKU);
			updatedProduct.setName("Updated Name");
			updatedProduct.setCategory("Clothing"); // changed category
			updatedProduct.setCreatedAt(product.getCreatedAt());
			updatedProduct.setUpdatedAt(Instant.now());
			ProductUpdateRequest categoryChangeRequest = new ProductUpdateRequest("Updated Name", null, null, null);
			ProductByCategory oldProjection = ProductByCategory.builder().key(ProductByCategoryKey.builder().category("Electronics").createdAt(product.getCreatedAt()).sku(SKU).build()).build();
			ProductByCategory newProjection = ProductByCategory.builder().key(ProductByCategoryKey.builder().category("Clothing").createdAt(product.getCreatedAt()).sku(SKU).build()).build();
			when(productRepository.findById(PRODUCT_ID)).thenReturn(Mono.just(product));
			// first call = old projection (before update), second call = new projection (after update)
			when(productMapper.toProductByCategoryView(any(Product.class))).thenReturn(oldProjection).thenReturn(newProjection);
			when(productRepository.save(any(Product.class))).thenReturn(Mono.just(updatedProduct));
			when(productByCategoryRepository.delete(oldProjection)).thenReturn(Mono.empty());
			when(productByCategoryRepository.save(newProjection)).thenReturn(Mono.just(newProjection));
			when(cacheService.evictProduct(CACHE_KEY)).thenReturn(Mono.empty());
			when(productMapper.toResponse(updatedProduct)).thenReturn(productResponse);
			StepVerifier.create(productService.update(PRODUCT_ID, categoryChangeRequest)).assertNext(r -> assertThat(r).isNotNull()).verifyComplete();
			verify(productByCategoryRepository).delete(oldProjection);
			verify(productByCategoryRepository).save(newProjection);
		}

		@Test
		@DisplayName("returns error when product not found")
		void returnsErrorWhenNotFound() {
			when(productRepository.findById(PRODUCT_ID)).thenReturn(Mono.empty());
			StepVerifier.create(productService.update(PRODUCT_ID, updateRequest)).expectErrorMessage("Product not found").verify();
		}

		@Test
		@DisplayName("completes successfully even when cache eviction fails")
		void toleratesCacheEvictionFailure() {
			ProductByCategory projection = ProductByCategory.builder().key(ProductByCategoryKey.builder().category("Electronics").createdAt(product.getCreatedAt()).sku(SKU).build()).build();
			when(productRepository.findById(PRODUCT_ID)).thenReturn(Mono.just(product));
			when(productMapper.toProductByCategoryView(any(Product.class))).thenReturn(projection);
			when(productRepository.save(any(Product.class))).thenReturn(Mono.just(product));
			when(productByCategoryRepository.save(projection)).thenReturn(Mono.just(projection));
			when(cacheService.evictProduct(CACHE_KEY)).thenReturn(Mono.error(new RuntimeException("Redis down")));
			when(productMapper.toResponse(product)).thenReturn(productResponse);
			StepVerifier.create(productService.update(PRODUCT_ID, updateRequest)).assertNext(r -> assertThat(r).isNotNull()).verifyComplete();
		}
	}
	// ── delete ────────────────────────────────────────────────────────────────

	@Nested
	@DisplayName("delete")
	class Delete {

		@Test
		@DisplayName("deletes product from all tables and evicts cache")
		void deletesProductAndEvictsCache() {
			when(productRepository.findById(PRODUCT_ID)).thenReturn(Mono.just(product));
			when(productRepository.deleteById(PRODUCT_ID)).thenReturn(Mono.empty());
			when(productByCategoryRepository.deleteById(any(ProductByCategoryKey.class))).thenReturn(Mono.empty());
			when(cacheService.evictProduct(CACHE_KEY)).thenReturn(Mono.empty());
			StepVerifier.create(productService.delete(PRODUCT_ID)).verifyComplete();
			verify(productRepository).deleteById(PRODUCT_ID);
			verify(productByCategoryRepository).deleteById(any(ProductByCategoryKey.class));
			verify(cacheService).evictProduct(CACHE_KEY);
		}

		@Test
		@DisplayName("returns error when product not found")
		void returnsErrorWhenNotFound() {
			when(productRepository.findById(PRODUCT_ID)).thenReturn(Mono.empty());
			StepVerifier.create(productService.delete(PRODUCT_ID)).expectErrorMessage("Product not found").verify();
			verify(productRepository, never()).deleteById(any(UUID.class));
		}

		@Test
		@DisplayName("completes successfully even when cache eviction fails")
		void toleratesCacheEvictionFailure() {
			when(productRepository.findById(PRODUCT_ID)).thenReturn(Mono.just(product));
			when(productRepository.deleteById(PRODUCT_ID)).thenReturn(Mono.empty());
			when(productByCategoryRepository.deleteById(any(ProductByCategoryKey.class))).thenReturn(Mono.empty());
			when(cacheService.evictProduct(CACHE_KEY)).thenReturn(Mono.error(new RuntimeException("Redis down")));
			StepVerifier.create(productService.delete(PRODUCT_ID)).verifyComplete();
		}
	}
}