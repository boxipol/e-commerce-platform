package com.pd.ecommerce.controller;

import com.pd.ecommerce.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;


@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public final class OrderController {

	private final OrderService orderService;


	@GetMapping("/fetch-data")
	public Mono<String> fetchData() {
		return orderService.getData();
	}

//	@GetMapping("/{id}")
//	public ResponseEntity<OrderResponse> getOrder(@PathVariable Long id) {
//		return ResponseEntity.ok(orderService.getOrder(id));
//	}
//
//	@PostMapping
//	public ResponseEntity<OrderResponse> placeOrder(@RequestBody OrderRequest order) {
//		return ResponseEntity.status(HttpStatus.CREATED)
//			.body(orderService.placeOrder(order));
//	}
}