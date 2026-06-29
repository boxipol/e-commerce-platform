package com.pd.ecommerce.kafka;

import com.pd.ecommerce.event.InventoryReservationCompletedEvent;
import com.pd.ecommerce.event.InventoryReservationFailedEvent;
import com.pd.ecommerce.event.OrderEventItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public final class InventoryEventProducer {

	private static final String COMPLETED_TOPIC = "reservation.completed";
	private static final String FAILED_TOPIC = "reservation.failed";

	private final KafkaTemplate<String, Object> kafkaTemplate;


	public Mono<Void> sendInventoryReserved(UUID orderId, List<OrderEventItem> items) {
		InventoryReservationCompletedEvent event = new InventoryReservationCompletedEvent(orderId, items);

		return Mono.fromFuture(
				kafkaTemplate.send(
					COMPLETED_TOPIC,
					orderId.toString(),
					event
				)
			)
			.doOnSuccess(result ->
				log.info(
					"Published InventoryReservationCompletedEvent orderId={}, topic={}, partition={}, offset={}",
					orderId,
					result.getRecordMetadata().topic(),
					result.getRecordMetadata().partition(),
					result.getRecordMetadata().offset()
				)
			)
			.doOnError(error ->
				log.error("Failed to publish InventoryReservationCompletedEvent orderId={}", orderId, error)
			)
			.then();
	}

	public Mono<Void> sendInventoryFailed(InventoryReservationFailedEvent event) {
		return Mono.fromFuture(
				kafkaTemplate.send(
					FAILED_TOPIC,
					event.orderId().toString(),
					event
				)
			)
			.doOnSuccess(result ->
				log.info(
					"Published InventoryReservationFailedEvent orderId={}, paymentId={}, topic={}, partition={}, offset={}",
					event.orderId(),
					event.paymentId(),
					result.getRecordMetadata().topic(),
					result.getRecordMetadata().partition(),
					result.getRecordMetadata().offset()
				)
			)
			.doOnError(error ->
				log.error(
					"Failed to publish InventoryReservationFailedEvent orderId={}, paymentId={}",
					event.orderId(),
					event.paymentId(),
					error
				)
			)
			.then();
	}
}