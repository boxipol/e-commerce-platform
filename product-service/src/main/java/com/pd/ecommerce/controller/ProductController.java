package com.pd.ecommerce.controller;

import com.pd.ecommerce.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;


@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public final class ProductController {

	private final ProductService productService;


	@GetMapping("/fetch-data")
	public Mono<String> fetchData() {
		return productService.getData();
	}
}