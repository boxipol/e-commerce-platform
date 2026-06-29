package com.pd.ecommerce.kafka;

import com.pd.ecommerce.event.InventoryReservationCompletedEvent;
import com.pd.ecommerce.service.ProductStockSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class InventoryReservationCompletedConsumer {

	private final ProductStockSyncService productStockSyncService;


	@KafkaListener(topics = "reservation.completed", groupId = "product-group")
	public Mono<Void> onReservationCompleted(InventoryReservationCompletedEvent event) {
		return productStockSyncService.decreaseStockForReservation(event.orderId(), event.items())
			.doOnSuccess(unused ->
				log.info("Applied reservation.completed stock sync for orderId={}", event.orderId())
			)
			.doOnError(error ->
				log.error("Failed applying reservation.completed stock sync for orderId={}", event.orderId(), error)
			);
	}
}