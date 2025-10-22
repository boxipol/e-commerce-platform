package com.pd.ecommerce.controller;

import com.pd.ecommerce.client.PaymentServiceClient;
import com.pd.ecommerce.dto.OrderRequest;
import com.pd.ecommerce.dto.OrderResponse;
import com.pd.ecommerce.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

	private final PaymentServiceClient orderServiceClient;
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


//	@GetMapping("/{id}")
//	public ResponseEntity<OrderResponse> getOrder(@PathVariable Long id) {
//		return ResponseEntity.ok(orderServiceClient.getOrder(id));
//	}


	@GetMapping("/hello")
	public String hello() {
		return "Hello from Order Service!";
	}

	@Bean
	@LoadBalanced // this enables Eureka-aware resolution
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}
}