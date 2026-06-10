package com.pd.ecommerce.service;

import com.pd.ecommerce.dto.InventoryCreateRequest;
import com.pd.ecommerce.dto.InventoryResponse;
import com.pd.ecommerce.dto.InventoryUpdateRequest;
import com.pd.ecommerce.event.PaymentCompletedEvent;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.List;
import java.util.UUID;

public interface InventoryService {

	Mono<InventoryResponse> create(InventoryCreateRequest request);
	Mono<InventoryResponse> getById(UUID productId);
	Flux<InventoryResponse> getProducts(List<UUID> ids);
	Mono<InventoryResponse> update(UUID productId, InventoryUpdateRequest request);
	Mono<Void> delete(UUID productId);

	Mono<Void> reserveInventory(PaymentCompletedEvent event);
}