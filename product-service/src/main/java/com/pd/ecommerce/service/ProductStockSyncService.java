package com.pd.ecommerce.service;

import com.pd.ecommerce.entity.ProductByCategory;
import com.pd.ecommerce.entity.ProductByCategoryKey;
import com.pd.ecommerce.event.OrderEventItem;
import com.pd.ecommerce.repository.ProductByCategoryRepository;
import com.pd.ecommerce.repository.ProductBySkuRepository;
import com.pd.ecommerce.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductStockSyncService {

	private final ProductBySkuRepository productBySkuRepository;
	private final ProductRepository productRepository;
	private final ProductByCategoryRepository productByCategoryRepository;
	private final ProductCacheService cacheService;


	public Mono<Void> decreaseStockForReservation(UUID orderId, List<OrderEventItem> items) {
		if (items == null || items.isEmpty()) {
			log.info("No items to apply for reservation.completed orderId={}", orderId);
			return Mono.empty();
		}

		return Flux.fromIterable(items)
			.concatMap(item -> decreaseSingleItem(orderId, item))
			.then();
	}



	private Mono<Void> decreaseSingleItem(UUID orderId, OrderEventItem item) {
		if (item.quantity() <= 0) {
			return Mono.error(new IllegalArgumentException(
				"Invalid quantity in reservation.completed orderId=%s sku=%s quantity=%d"
					.formatted(orderId, item.sku(), item.quantity())
			));
		}

		return productBySkuRepository.findById(item.sku())
			.switchIfEmpty(Mono.error(new IllegalStateException(
				"Product not found by sku for reservation.completed: " + item.sku()
			)))
			.flatMap(productBySku -> {
				Integer currentStock = productBySku.getStock();

				if (currentStock == null) {
					return Mono.error(new IllegalStateException("Stock is null for sku: " + item.sku()));
				}

				if (currentStock < item.quantity()) {
					return Mono.error(new IllegalStateException(
						"Insufficient product stock in product-service for sku %s: current=%d requested=%d"
							.formatted(item.sku(), currentStock, item.quantity())
					));
				}

				int newStock = currentStock - item.quantity();
				Instant now = Instant.now();
				productBySku.setStock(newStock);
				productBySku.setUpdatedAt(now);

				return productBySkuRepository.save(productBySku)
					.flatMap(savedSku ->
						productRepository.findById(savedSku.getProductId())
							.switchIfEmpty(Mono.error(new IllegalStateException(
								"Product not found by id: " + savedSku.getProductId()
							)))
							.flatMap(product -> {
								product.setStock(newStock);
								product.setUpdatedAt(now);

								return productRepository.save(product);
							})
							.then(productByCategoryRepository.save(
								ProductByCategory.builder()
									.key(ProductByCategoryKey.builder()
										.category(savedSku.getCategory())
										.createdAt(savedSku.getCreatedAt())
										.sku(savedSku.getSku())
										.build())
									.name(savedSku.getName())
									.brand(savedSku.getBrand())
									.price(savedSku.getPrice())
									.stock(newStock)
									.build()
							))
							.then(
								cacheService.evictProduct("product:" + savedSku.getSku())
							)
							.then()
					);
			});
	}
}