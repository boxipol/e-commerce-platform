package com.pd.ecommerce.controller;

import com.pd.ecommerce.entity.Product;
import com.pd.ecommerce.entity.ProductByCategory;
import com.pd.ecommerce.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public final class ProductController {

	private final ProductService productService;


	@GetMapping("/{id}")
	public Mono<Product> getById(@PathVariable UUID id) {
		return productService.getById(id);
	}

	@GetMapping
	public Flux<Product> getAll() {
		return productService.getAll();
	}

	@PostMapping
	public Mono<Product> create(@RequestBody Product product) {
		return productService.create(product);
	}

	@DeleteMapping("/{id}")
	public Mono<Void> delete(@PathVariable UUID id) {
		return productService.delete(id);
	}

	@GetMapping("/{category}/products")
	public Flux<ProductByCategory> getProducts(@PathVariable String category) {
		return productService.getByCategory(category);
	}
}