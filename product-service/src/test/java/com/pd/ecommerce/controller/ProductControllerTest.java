package com.pd.ecommerce.controller;

import com.pd.ecommerce.dto.ProductByCategoryView;
import com.pd.ecommerce.dto.ProductCreateRequest;
import com.pd.ecommerce.dto.ProductPageResponse;
import com.pd.ecommerce.dto.ProductResponse;
import com.pd.ecommerce.dto.ProductUpdateRequest;
import com.pd.ecommerce.exception.GlobalExceptionHandler;
import com.pd.ecommerce.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// Include GlobalExceptionHandler so the slice handles WebExchangeBindException
// the same way production does (400 from the dedicated handler, not 500 from the catch-all).
@WebFluxTest({ProductController.class, GlobalExceptionHandler.class})
@DisplayName("ProductController")
class ProductControllerTest {

	@Autowired
	private WebTestClient webClient;

	@MockitoBean
	private ProductService productService;

	private static final String SKU = "BRAND-PROD-ABC123";
	private static final UUID PRODUCT_ID = UUID.randomUUID();

	private ProductResponse productResponse;


	@BeforeEach
	void setUp() {
		productResponse = new ProductResponse(PRODUCT_ID, SKU, "Test Product", "A test product", "Brand", "Electronics", new BigDecimal("99.99"), "USD", true, Instant.now());
	}
	// ── GET /api/v1/products/{sku} ────────────────────────────────────────────

	@Nested
	@DisplayName("GET /api/v1/products/{sku}")
	class GetBySku {
		@Test
		@DisplayName("returns 200 with product response")
		void returns200WithProduct() {
			when(productService.getBySku(SKU)).thenReturn(Mono.just(productResponse));
			webClient.get().uri("/api/v1/products/{sku}", SKU).exchange().expectStatus().isOk().expectBody(ProductResponse.class).value(r -> {
				assert r.sku().equals(SKU);
				assert r.name().equals("Test Product");
			});
		}

		@Test
		@DisplayName("returns 200 with empty body when product not found")
		void returns200EmptyWhenNotFound() {
			when(productService.getBySku(SKU)).thenReturn(Mono.empty());
			webClient.get().uri("/api/v1/products/{sku}", SKU).exchange().expectStatus().isOk().expectBody().isEmpty();
		}
	}
	// ── GET /api/v1/products/batch ────────────────────────────────────────────

	@Nested
	@DisplayName("GET /api/v1/products/batch")
	class GetBatch {
		@Test
		@DisplayName("returns 200 with all matching products")
		void returns200WithProducts() {
			when(productService.getProducts(List.of(SKU))).thenReturn(Flux.just(productResponse));
			webClient.get().uri(u -> u.path("/api/v1/products/batch").queryParam("skus", SKU).build()).exchange().expectStatus().isOk().expectBodyList(ProductResponse.class).hasSize(1);
		}
	}
	// ── GET /api/v1/products/category/{category} ──────────────────────────────

	@Nested
	@DisplayName("GET /api/v1/products/category/{category}")
	class GetByCategory {
		@Test
		@DisplayName("returns 200 with paginated product list")
		void returns200WithPage() {
			ProductByCategoryView view = ProductByCategoryView.builder().sku(SKU).name("Test Product").brand("Brand").price(new BigDecimal("99.99")).stock(10).build();
			ProductPageResponse page = new ProductPageResponse(List.of(view), null);
			when(productService.getByCategory(eq("Electronics"), eq(20), eq(null))).thenReturn(Mono.just(page));
			webClient.get().uri("/api/v1/products/category/Electronics").exchange().expectStatus().isOk().expectBody(ProductPageResponse.class).value(r -> {
				assert r.items().size() == 1;
			});
		}

		@Test
		@DisplayName("passes custom pageSize and pageState query params")
		void passesCustomPaginationParams() {
			ProductPageResponse page = new ProductPageResponse(List.of(), "nextCursor");
			when(productService.getByCategory("Electronics", 10, "someCursor")).thenReturn(Mono.just(page));
			webClient.get().uri(u -> u.path("/api/v1/products/category/Electronics").queryParam("pageSize", 10).queryParam("pageState", "someCursor").build()).exchange().expectStatus().isOk().expectBody(ProductPageResponse.class).value(r -> {
				assert "nextCursor".equals(r.cursor());
			});
		}
	}
	// ── POST /api/v1/products ─────────────────────────────────────────────────

	@Nested
	@DisplayName("POST /api/v1/products")
	class Create {
		private ProductCreateRequest validRequest;

		@BeforeEach
		void setUp() {
			validRequest = new ProductCreateRequest(SKU, "Test Product", "A test product", "Brand", "Electronics", new BigDecimal("99.99"), "USD", 10);
		}

		@Test
		@DisplayName("returns 200 with created product")
		void returns200WithCreatedProduct() {
			when(productService.create(any(ProductCreateRequest.class))).thenReturn(Mono.just(productResponse));
			webClient.post().uri("/api/v1/products").contentType(MediaType.APPLICATION_JSON).bodyValue(validRequest).exchange().expectStatus().isOk().expectBody(ProductResponse.class).value(r -> {
				assert r.sku().equals(SKU);
				assert r.available();
			});
		}

		@Test
		@DisplayName("returns 400 when required fields are missing")
		void returns400WhenInvalid() {
			// name is @NotBlank — send empty string to trigger validation
			ProductCreateRequest invalidRequest = new ProductCreateRequest("", "", null, "", "", null, "", null);
			webClient.post().uri("/api/v1/products").contentType(MediaType.APPLICATION_JSON).bodyValue(invalidRequest).exchange().expectStatus().isBadRequest();
		}
	}
	// ── PATCH /api/v1/products/{id} ───────────────────────────────────────────

	@Nested
	@DisplayName("PATCH /api/v1/products/{id}")
	class Update {
		@Test
		@DisplayName("returns 200 with updated product")
		void returns200WithUpdatedProduct() {
			ProductUpdateRequest updateRequest = new ProductUpdateRequest("Updated Name", null, new BigDecimal("149.99"), null);
			when(productService.update(eq(PRODUCT_ID), any(ProductUpdateRequest.class))).thenReturn(Mono.just(productResponse));
			webClient.patch().uri("/api/v1/products/{id}", PRODUCT_ID).contentType(MediaType.APPLICATION_JSON).bodyValue(updateRequest).exchange().expectStatus().isOk().expectBody(ProductResponse.class).value(r -> {
				assert r.productId().equals(PRODUCT_ID);
			});
		}

		@Test
		@DisplayName("returns 500 when product not found")
		void returns500WhenNotFound() {
			ProductUpdateRequest updateRequest = new ProductUpdateRequest("Updated Name", null, null, null);
			when(productService.update(eq(PRODUCT_ID), any(ProductUpdateRequest.class))).thenReturn(Mono.error(new RuntimeException("Product not found")));
			webClient.patch().uri("/api/v1/products/{id}", PRODUCT_ID).contentType(MediaType.APPLICATION_JSON).bodyValue(updateRequest).exchange().expectStatus().is5xxServerError();
		}
	}
	// ── DELETE /api/v1/products/{id} ──────────────────────────────────────────

	@Nested
	@DisplayName("DELETE /api/v1/products/{id}")
	class Delete {
		@Test
		@DisplayName("returns 200 on successful deletion")
		void returns200OnSuccess() {
			when(productService.delete(PRODUCT_ID)).thenReturn(Mono.empty());
			webClient.delete().uri("/api/v1/products/{id}", PRODUCT_ID).exchange().expectStatus().isOk();
			verify(productService).delete(PRODUCT_ID);
		}

		@Test
		@DisplayName("returns 500 when product not found")
		void returns500WhenNotFound() {
			when(productService.delete(PRODUCT_ID)).thenReturn(Mono.error(new RuntimeException("Product not found")));
			webClient.delete().uri("/api/v1/products/{id}", PRODUCT_ID).exchange().expectStatus().is5xxServerError();
		}
	}
}