package com.pd.ecommerce.repository;

import com.pd.ecommerce.entity.OutboxEvent;
import com.pd.ecommerce.entity.OutboxEventStatus;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.time.Instant;
import java.util.UUID;

public interface OutboxEventRepository extends ReactiveCrudRepository<OutboxEvent, UUID> {

	Flux<OutboxEvent> findByStatus(OutboxEventStatus status);

	@Query("""
		    UPDATE outbox_events
		    SET status = 'PROCESSED',
		        updated_at = :updatedAt
		    WHERE aggregate_id = :orderId
		      AND status = 'PROCESSING'
		""")
	Mono<Integer> markProcessed(UUID orderId, Instant updatedAt);

	@Query("""
		    UPDATE outbox_events
		    SET status = 'FAILED',
		        updated_at = :updatedAt
		    WHERE aggregate_id = :orderId
		""")
	Mono<Integer> markAsFailed(UUID orderId, Instant updatedAt);
}