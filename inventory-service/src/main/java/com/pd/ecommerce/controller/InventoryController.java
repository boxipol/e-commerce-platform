package com.pd.ecommerce.controller;

import com.pd.ecommerce.dto.InventoryCreateRequest;
import com.pd.ecommerce.dto.InventoryResponse;
import com.pd.ecommerce.dto.InventoryUpdateRequest;
import com.pd.ecommerce.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "Inventory", description = "Stock level management for product SKUs")
@RestController
@RequestMapping("/api/v1/inventories")
@RequiredArgsConstructor
public final class InventoryController {

	private final InventoryService service;


	@Operation(summary = "Get inventory item by ID", description = "Returns stock details for a single inventory record")
	@GetMapping("/{id}")
	public Mono<InventoryResponse> getById(@PathVariable UUID id) {
		return service.get(id);
	}

	@Operation(summary = "Get inventory items by IDs (batch)", description = "Returns stock details for multiple inventory records")
	@GetMapping("/batch")
	public Flux<InventoryResponse> getProducts(@RequestParam List<UUID> ids) {
		return service.get(ids);
	}

	@Operation(summary = "Create inventory record", description = "Registers a new product SKU with initial stock level")
	@PostMapping
	public Mono<InventoryResponse> create(@Valid @RequestBody InventoryCreateRequest request) {
		return service.create(request);
	}

	@Operation(summary = "Update inventory record", description = "Adjusts stock level or other attributes of an existing inventory record")
	@PatchMapping("/{id}")
	public Mono<InventoryResponse> update(@PathVariable UUID id, @Valid @RequestBody InventoryUpdateRequest request) {
		return service.update(id, request);
	}

	@Operation(summary = "Delete inventory record", description = "Removes an inventory record permanently")
	@DeleteMapping("/{id}")
	public Mono<Void> delete(@PathVariable UUID id) {
		return service.delete(id);
	}
}