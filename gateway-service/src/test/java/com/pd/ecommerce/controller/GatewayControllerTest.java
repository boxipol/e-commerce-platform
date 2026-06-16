package com.pd.ecommerce.controller;

import com.pd.ecommerce.service.GatewayServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("GatewayController Tests")
class GatewayControllerTest {

	@Mock
	private GatewayServiceImpl gatewayService;

	@InjectMocks
	private GatewayController gatewayController;


	@Test
	@DisplayName("fetchData - should delegate to the gateway service")
	void testFetchData() {
		when(gatewayService.getData()).thenReturn(Mono.just("Gateway Service is up and running!"));

		StepVerifier.create(gatewayController.fetchData())
			.expectNext("Gateway Service is up and running!")
			.verifyComplete();
	}
}