package com.pd.ecommerce.kafka;

import com.pd.ecommerce.event.InventoryReservationCompletedEvent;
import com.pd.ecommerce.event.InventoryReservationFailedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public final class InventoryEventProducer {

	private static final String COMPLETED_TOPIC = "reservation.completed";
	private static final String FAILED_TOPIC = "reservation.failed";

	private final KafkaTemplate<String, Object> kafkaTemplate;


	public Mono<Void> sendInventoryReserved(UUID orderId) {
		InventoryReservationCompletedEvent event = new InventoryReservationCompletedEvent(orderId);

		return Mono.fromFuture(
				kafkaTemplate.send(
					COMPLETED_TOPIC,
					orderId.toString(),
					event
				)
			)
			.doOnSuccess(result ->
				log.info("Published InventoryReservationCompletedEvent orderId={}", orderId)
			)
			.then();
	}

	public Mono<Void> sendInventoryFailed(UUID orderId, UUID paymentId, String message) {
		InventoryReservationFailedEvent event = InventoryReservationFailedEvent.builder()
			.orderId(orderId)
			.paymentId(paymentId)
			.reason(message)
			.build();

		return Mono.fromFuture(
				kafkaTemplate.send(
					FAILED_TOPIC,
					orderId.toString(),
					event
				)
			)
			.doOnSuccess(result ->
				log.info("Published InventoryReservationFailedEvent orderId={}", orderId)
			)
			.then();
	}
}