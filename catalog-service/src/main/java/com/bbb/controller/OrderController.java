package com.bbb.controller;

import lombok.RequiredArgsConstructor;
import com.bbb.dto.OrderRequest;
import com.bbb.dto.OrderResponse;
import com.bbb.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

	private final OrderService orderService;


	@GetMapping("/{id}")
	public ResponseEntity<OrderResponse> getOrder(@PathVariable Long id) {
		return ResponseEntity.ok(orderService.getOrder(id));
	}

	@PostMapping
	public ResponseEntity<OrderResponse> placeOrder(@RequestBody OrderRequest order) {
		return ResponseEntity.status(HttpStatus.CREATED)
			.body(orderService.placeOrder(order));
	}
}