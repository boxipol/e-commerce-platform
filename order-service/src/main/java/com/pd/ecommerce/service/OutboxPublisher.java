package com.pd.ecommerce.service;

import com.pd.ecommerce.entity.OutboxEvent;
import com.pd.ecommerce.entity.OutboxStatus;
import com.pd.ecommerce.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxPublisher {

	private final OutboxEventRepository outboxRepository;
	private final KafkaTemplate<String, String> kafkaTemplate;


	@Scheduled(fixedDelay = 2000)
	public void publishOutboxEvents() {
		outboxRepository.findByStatus(OutboxStatus.PENDING)
			.flatMap(this::processEvent)
			.onErrorContinue((err, obj) -> log.error("Outbox processing failed for event {}", obj, err))
			.subscribe();
	}

	private Mono<Void> processEvent(OutboxEvent event) {
		return markProcessing(event).flatMap(this::publishToKafka)
			.flatMap(this::markPublished).then();
	}

	private Mono<OutboxEvent> markProcessing(OutboxEvent event) {
		event.setStatus(OutboxStatus.PROCESSING);
		return outboxRepository.save(event);
	}

	private Mono<OutboxEvent> publishToKafka(OutboxEvent event) {
		return Mono.fromCallable(() -> {
			String topic = event.getAggregateType().toLowerCase() + ".events";
			kafkaTemplate.send(topic, event.getPayload()).get(); // blocking wait
			log.info("Published event {} to topic {}", event.getId(), topic);

			return event;
		}).subscribeOn(Schedulers.boundedElastic());
	}

	private Mono<OutboxEvent> markPublished(OutboxEvent event) {
		event.setStatus(OutboxStatus.PUBLISHED);
		event.setPublishedAt(Instant.now());

		return outboxRepository.save(event);
	}
}