package com.pd.ecommerce.service;

import com.pd.ecommerce.dto.ItemUpdateRequest;
import reactor.core.publisher.Mono;

public interface InventoryService {

	Mono<Void> update(ItemUpdateRequest request);
}