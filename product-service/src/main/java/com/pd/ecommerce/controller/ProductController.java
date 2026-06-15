package com.pd.ecommerce.controller;

import com.pd.ecommerce.dto.ProductCreateRequest;
import com.pd.ecommerce.dto.ProductPageResponse;
import com.pd.ecommerce.dto.ProductResponse;
import com.pd.ecommerce.dto.ProductUpdateRequest;
import com.pd.ecommerce.service.ProductService;
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

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public final class ProductController {

	private final ProductService productService;


	@GetMapping("/{sku}")
	public Mono<ProductResponse> getById(@PathVariable String sku) {
		return productService.getBySku(sku);
	}

	@GetMapping("/batch")
	public Flux<ProductResponse> getProducts(@RequestParam List<String> skus) {
		return productService.getProducts(skus);
	}

	@GetMapping("/category/{category}")
	public Mono<ProductPageResponse> getByCategory(
		@PathVariable String category,
		@RequestParam(defaultValue = "20") int pageSize,
		@RequestParam(required = false) String pageState
	) {
		return productService.getByCategory(category, pageSize, pageState);
	}

	@PostMapping
	public Mono<ProductResponse> create(@Valid @RequestBody ProductCreateRequest request) {
		return productService.create(request);
	}

	@PatchMapping("/{id}")
	public Mono<ProductResponse> update(@PathVariable UUID id, @Valid @RequestBody ProductUpdateRequest request) {
		return productService.update(id, request);
	}

	@DeleteMapping("/{id}")
	public Mono<Void> delete(@PathVariable UUID id) {
		return productService.delete(id);
	}
}