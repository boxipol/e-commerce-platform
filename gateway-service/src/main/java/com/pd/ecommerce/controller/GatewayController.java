package com.pd.ecommerce.controller;

import com.pd.ecommerce.service.GatewayServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
public final class GatewayController {

	private final GatewayServiceImpl gatewayService;


	@GetMapping("/fetch-data")
	public Mono<String> fetchData() {
		return gatewayService.getData();
	}
}