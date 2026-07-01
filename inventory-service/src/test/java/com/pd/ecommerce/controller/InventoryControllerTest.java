package com.pd.ecommerce.controller;

import com.pd.ecommerce.dto.InventoryCreateRequest;
import com.pd.ecommerce.dto.InventoryResponse;
import com.pd.ecommerce.dto.InventoryUpdateRequest;
import com.pd.ecommerce.service.InventoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("InventoryController Tests")
class InventoryControllerTest {

	@Mock
	private InventoryService service;

	@InjectMocks
	private InventoryController controller;

	private UUID productId;
	private InventoryResponse response;


	@BeforeEach
	void setUp() {
		productId = UUID.randomUUID();
		response = InventoryResponse.builder().productId(productId).quantity(10).build();
	}

	@Test
	@DisplayName("getById - should delegate to service")
	void testGetById() {
		when(service.get(productId)).thenReturn(Mono.just(response));

		StepVerifier.create(controller.getById(productId))
			.assertNext(r -> assertThat(r.productId()).isEqualTo(productId))
			.verifyComplete();
	}

	@Test
	@DisplayName("getProducts - should delegate batch lookup to service")
	void testGetProducts() {
		List<UUID> ids = List.of(productId);
		when(service.get(ids)).thenReturn(Flux.just(response));

		StepVerifier.create(controller.getProducts(ids))
			.expectNextCount(1)
			.verifyComplete();
	}

	@Test
	@DisplayName("create - should delegate to service")
	void testCreate() {
		InventoryCreateRequest request = new InventoryCreateRequest(productId, 10);
		when(service.create(request)).thenReturn(Mono.just(response));

		StepVerifier.create(controller.create(request))
			.expectNextCount(1)
			.verifyComplete();

		verify(service).create(request);
	}

	@Test
	@DisplayName("update - should delegate to service")
	void testUpdate() {
		InventoryUpdateRequest request = new InventoryUpdateRequest(20);
		when(service.update(productId, request)).thenReturn(Mono.just(response));

		StepVerifier.create(controller.update(productId, request))
			.expectNextCount(1)
			.verifyComplete();

		verify(service).update(productId, request);
	}

	@Test
	@DisplayName("delete - should delegate to service")
	void testDelete() {
		when(service.delete(productId)).thenReturn(Mono.empty());

		StepVerifier.create(controller.delete(productId)).verifyComplete();

		verify(service).delete(productId);
	}
}