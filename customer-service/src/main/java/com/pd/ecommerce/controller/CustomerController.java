package com.pd.ecommerce.controller;

import com.pd.ecommerce.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
public final class CustomerController {

	private final CustomerService customerService;


	@GetMapping("/fetch-data")
	public Mono<String> fetchData() {
		return customerService.getData();
	}
}