package com.pd.ecommerce.service;

import com.pd.ecommerce.dto.ItemUpdateRequest;
import com.pd.ecommerce.kafka.ItemEventProducer;
import com.pd.ecommerce.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
final class InventoryServiceImpl implements InventoryService {

	private final ItemRepository repository;
	private final ItemEventProducer eventProducer;


	@Override
	public Mono<Void> update(ItemUpdateRequest request) {
		return null;
	}
}