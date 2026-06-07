package com.pd.ecommerce.kafka;

import com.pd.ecommerce.event.InventoryReservationCompletedEvent;
import com.pd.ecommerce.event.InventoryReservationFailedEvent;
import com.pd.ecommerce.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
final class InventoryEventConsumer {

	private final OrderService orderService;


	@KafkaListener(topics = "reservation.completed")
	public Mono<Void> onReservationCompleted(InventoryReservationCompletedEvent event) {
		return orderService.markAsPaid(event.orderId())
			.doOnSuccess(response ->
				log.info("Reservation completed for order {}", event.orderId())
			)
			.doOnError(error ->
				log.error("Failed completing order {}", event.orderId(), error)
			);
	}

	@KafkaListener(topics = "reservation.failed")
	public Mono<Void> onReservationFailed(InventoryReservationFailedEvent event) {
		return orderService.markAsFailed(event.orderId())
			.doOnSuccess(response ->
				log.info("Reservation failed for order {}", event.orderId())
			)
			.doOnError(error ->
				log.error("Failed completing order {}", event.orderId(), error)
			);
	}
}
