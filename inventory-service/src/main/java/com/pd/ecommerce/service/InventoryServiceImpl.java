package com.pd.ecommerce.service;

import com.pd.ecommerce.event.OrderItem;
import com.pd.ecommerce.event.PaymentCompletedEvent;
import com.pd.ecommerce.exception.InsufficientInventoryException;
import com.pd.ecommerce.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

	private final InventoryRepository repository;


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