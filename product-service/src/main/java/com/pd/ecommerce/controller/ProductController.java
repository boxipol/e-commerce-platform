package com.pd.ecommerce.controller;

import com.pd.ecommerce.dto.ProductCreateRequest;
import com.pd.ecommerce.dto.ProductPageResponse;
import com.pd.ecommerce.dto.ProductResponse;
import com.pd.ecommerce.dto.ProductUpdateRequest;
import com.pd.ecommerce.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.List;
import java.util.UUID;

@Tag(name = "Products", description = "Product catalog management")
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public final class ProductController {

	private final ProductService productService;


	@Operation(summary = "Get product by SKU", description = "Returns a single product from cache or Cassandra")
	@GetMapping("/{sku}")
	public Mono<ProductResponse> getById(@PathVariable String sku) {
		return productService.getBySku(sku);
	}

	@Operation(summary = "Get products by SKUs (batch)", description = "Returns multiple products in parallel from cache or Cassandra")
	@GetMapping("/batch")
	public Flux<ProductResponse> getProducts(@RequestParam List<String> skus) {
		return productService.getProducts(skus);
	}

	@Operation(summary = "Get products by category (paginated)", description = "Returns a cursor-paginated page of products for a given category")
	@GetMapping("/category/{category}")
	public Mono<ProductPageResponse> getByCategory(
		@PathVariable String category,
		@RequestParam(defaultValue = "20") int pageSize,
		@RequestParam(required = false) String pageState
	) {
		return productService.getByCategory(category, pageSize, pageState);
	}

	@Operation(summary = "Create product", description = "Persists a new product to Cassandra (all lookup tables)")
	@PostMapping
	public Mono<ProductResponse> create(@Valid @RequestBody ProductCreateRequest request) {
		return productService.create(request);
	}

	@Operation(summary = "Update product", description = "Partial update of a product; evicts stale cache entry")
	@PatchMapping("/{id}")
	public Mono<ProductResponse> update(@PathVariable UUID id, @Valid @RequestBody ProductUpdateRequest request) {
		return productService.update(id, request);
	}

	@Operation(summary = "Delete product", description = "Removes product from all Cassandra tables and evicts cache")
	@DeleteMapping("/{id}")
	public Mono<Void> delete(@PathVariable UUID id) {
		return productService.delete(id);
	}
}