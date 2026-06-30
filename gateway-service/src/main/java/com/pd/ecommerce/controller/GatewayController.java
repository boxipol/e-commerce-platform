package com.pd.ecommerce.controller;

import com.pd.ecommerce.service.GatewayServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@Tag(name = "Gateway", description = "Gateway-level utilities")
@RestController
@RequiredArgsConstructor
public final class GatewayController {

	private final GatewayServiceImpl gatewayService;


	@Operation(summary = "Fetch data", description = "Internal gateway utility endpoint")
	@GetMapping("/fetch-data")
	public Mono<String> fetchData() {
		return gatewayService.getData();
	}
}