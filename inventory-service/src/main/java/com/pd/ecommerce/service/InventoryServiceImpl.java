package com.pd.ecommerce.service;

import com.pd.ecommerce.dto.InventoryCreateRequest;
import com.pd.ecommerce.dto.InventoryResponse;
import com.pd.ecommerce.dto.InventoryUpdateRequest;
import com.pd.ecommerce.entity.Inventory;
import com.pd.ecommerce.event.OrderItem;
import com.pd.ecommerce.event.PaymentCompletedEvent;
import com.pd.ecommerce.exception.InsufficientInventoryException;
import com.pd.ecommerce.exception.ProductAlreadyExistsException;
import com.pd.ecommerce.mapper.InventoryMapper;
import com.pd.ecommerce.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

	private final InventoryRepository repository;
	private final InventoryMapper mapper;


	@Override
	public Mono<InventoryResponse> getById(UUID productId) {
		return repository.findById(productId)
			.switchIfEmpty(
				Mono.error(new RuntimeException("Inventory not found"))
			)
			.map(mapper::toResponse);
	}

	@Override
	public Flux<InventoryResponse> getProducts(List<UUID> ids) {
		return Flux.fromIterable(ids)
			.flatMap(repository::findById, 32)
			.map(mapper::toResponse);
	}

	@Override
	public Mono<InventoryResponse> create(InventoryCreateRequest request) {
		Instant now = Instant.now();
		return repository.findById(request.productId())
			.flatMap(existing ->
				Mono.<Inventory>error(new ProductAlreadyExistsException(request.productId()))
			)
			.switchIfEmpty(
				Mono.defer(() -> repository.insert(request.productId(), request.quantity(), now, now)))
			.map(mapper::toResponse);
	}

	@Override
	public Mono<InventoryResponse> update(UUID productId, InventoryUpdateRequest request) {
		return repository.findById(productId)
			.switchIfEmpty(
				Mono.error(new RuntimeException("Inventory not found"))
			)
			.flatMap(existing -> {
				existing.setQuantity(request.quantity());
				existing.setUpdatedAt(Instant.now());

				return repository.update(existing.getProductId(), request.quantity(), existing.getUpdatedAt());
			})
			.map(mapper::toResponse);
	}

	@Override
	public Mono<Void> delete(UUID productId) {
		return repository.findById(productId)
			.switchIfEmpty(
				Mono.error(new RuntimeException("Inventory not found"))
			)
			.flatMap(repository::delete);
	}

	@Override
	@Transactional
	public Mono<Void> reserveInventory(PaymentCompletedEvent event) {
		return Flux.fromIterable(event.items())
			.concatMap(this::reserveItem)
			.then();
	}

//	==================== PRIVATE ====================

	private Mono<Void> reserveItem(OrderItem item) {
		Instant now = Instant.now();

		return repository.decreaseStock(item.productId(), item.quantity(), now)
			.flatMap(rowsUpdated -> rowsUpdated == 0
				? Mono.error(new InsufficientInventoryException("Not enough stock for product " + item.productId()))
				: Mono.empty()
			);
	}
}