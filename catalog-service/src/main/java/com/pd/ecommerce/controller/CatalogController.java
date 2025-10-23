package com.pd.ecommerce.controller;

import com.pd.ecommerce.dto.ProductResponse;
import com.pd.ecommerce.service.CatalogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class CatalogController {

	private final CatalogService catalogService;


	@GetMapping("/{id}")
	public ResponseEntity<ProductResponse> getProduct(@PathVariable Long id) {
		return ResponseEntity.ok(catalogService.getProduct(id));
	}

//	@PostMapping
//	public ResponseEntity<OrderResponse> placeOrder(@RequestBody OrderRequest order) {
//		return ResponseEntity.status(HttpStatus.CREATED)
//			.body(catalogService.placeOrder(order));
//	}
}