package com.pd.ecommerce.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pd.ecommerce.entity.OutboxEvent;
import com.pd.ecommerce.entity.OutboxEventStatus;
import com.pd.ecommerce.event.OrderCreatedEvent;
import com.pd.ecommerce.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Slf4j
@Component
@RequiredArgsConstructor
public final class OutboxPublisher {

	private final OutboxEventRepository outboxRepository;
	private final KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate;
	private final ObjectMapper objectMapper;


	@Scheduled(fixedDelay = 10_000)
	public void publishOutboxEvents() {
		outboxRepository.findByStatus(OutboxEventStatus.PENDING)
			.flatMap(this::processEvent)
			.onErrorContinue((err, obj) -> log.error("Outbox processing failed for event {}", obj, err))
			.subscribe();
	}

//	@Scheduled(fixedDelay = 60_000)
//	public void publishOutboxProcessingEvents() {
//		outboxRepository.findByStatus(OutboxEventStatus.PROCESSING)
//			.flatMap(this::processEvent)
//			.onErrorContinue((err, obj) -> log.error("Outbox processing failed for event {}", obj, err))
//			.subscribe();
//	}

//	==================== PRIVATE ====================

	private Mono<Void> processEvent(OutboxEvent event) {
		return markProcessing(event).flatMap(this::publishToKafka)
			.flatMap(this::markPublished)
			.then();
	}

	private Mono<OutboxEvent> markProcessing(OutboxEvent event) {
		event.setStatus(OutboxEventStatus.PROCESSING);
		return outboxRepository.save(event);
	}

	private Mono<OutboxEvent> publishToKafka(OutboxEvent event) {
		return Mono.fromCallable(() -> {
			String topic = "order.created";
			OrderCreatedEvent orderEvent = objectMapper.readValue(event.getPayload().asString(), OrderCreatedEvent.class);

			kafkaTemplate.send(topic, orderEvent)
				.get(); // blocking wait

			log.info("Published event {} to topic {}", event.getId(), topic);

			return event;
		}).subscribeOn(Schedulers.boundedElastic());
	}

	private Mono<OutboxEvent> markPublished(OutboxEvent event) {
		log.info("Marking {} as PROCESSING", event.getId());

		event.setStatus(OutboxEventStatus.PROCESSING);

		return outboxRepository.save(event)
			.doOnError(saved ->
				log.info("Error mark {} as PROCESSING", event.getId())
			);
	}
}