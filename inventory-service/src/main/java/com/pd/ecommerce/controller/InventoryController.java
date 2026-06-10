package com.pd.ecommerce.controller;

import com.pd.ecommerce.dto.InventoryCreateRequest;
import com.pd.ecommerce.dto.InventoryResponse;
import com.pd.ecommerce.dto.InventoryUpdateRequest;
import com.pd.ecommerce.service.InventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/inventories")
@RequiredArgsConstructor
public final class InventoryController {

	private final InventoryService service;


	@GetMapping("/{id}")
	public Mono<InventoryResponse> getById(@PathVariable UUID id) {
		return service.getById(id);
	}

	@GetMapping("/batch")
	public Flux<InventoryResponse> getProducts(@RequestParam List<UUID> ids) {
		return service.getProducts(ids);
	}

	@PostMapping
	public Mono<InventoryResponse> create(@Valid @RequestBody InventoryCreateRequest request) {
		return service.create(request);
	}

	@PatchMapping("/{id}")
	public Mono<InventoryResponse> update(@PathVariable UUID id, @Valid @RequestBody InventoryUpdateRequest request) {
		return service.update(id, request);
	}

	@DeleteMapping("/{id}")
	public Mono<Void> delete(@PathVariable UUID id) {
		return service.delete(id);
	}
}