package com.pd.ecommerce.controller;

import com.pd.ecommerce.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
public final class PaymentController {

	private final PaymentService paymentService;


	@GetMapping("/fetch-data")
	public Mono<String> fetchData() {
		return paymentService.getData();
	}
}