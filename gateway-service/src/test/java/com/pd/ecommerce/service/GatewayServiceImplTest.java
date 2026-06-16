package com.pd.ecommerce.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

@DisplayName("GatewayServiceImpl Tests")
class GatewayServiceImplTest {

	private final GatewayServiceImpl gatewayService = new GatewayServiceImpl();


	@Test
	@DisplayName("getData - should emit the status message")
	void testGetData() {
		StepVerifier.create(gatewayService.getData())
			.expectNext("Gateway Service is up and running!")
			.verifyComplete();
	}
}