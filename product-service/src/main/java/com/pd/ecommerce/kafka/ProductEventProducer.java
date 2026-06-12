package com.pd.ecommerce.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public final class ProductEventProducer {

	private static final String CREATED_TOPIC = "product.created";
	private static final String REMOVED_TOPIC = "product.removed";

	private final KafkaTemplate<String, Object> kafkaTemplate;


//	public Mono<Void> sendInventoryReserved(UUID orderId) {
//		ProductCreatedEvent event = new ProductCreatedEvent(orderId);
//
//		return Mono.fromFuture(
//				kafkaTemplate.send(CREATED_TOPIC,
//					orderId.toString(),
//					event
//				)
//			)
//			.doOnSuccess(result ->
//				log.info("Published ProductCreatedEvent productId={}", orderId)
//			)
//			.then();
//	}
//
//	public Mono<Void> sendInventoryFailed(UUID orderId, String message) {
//		ProductCreatedEvent event = new ProductCreatedEvent(orderId, message);
//
//		return Mono.fromFuture(
//				kafkaTemplate.send(REMOVED_TOPIC,
//					orderId.toString(),
//					event
//				)
//			)
//			.doOnSuccess(result ->
//				log.info("Published ProductCreatedEvent productId={}", orderId)
//			)
//			.then();
//	}
}