package com.pd.ecommerce.repository;

import com.pd.ecommerce.entity.OutboxEvent;
import com.pd.ecommerce.entity.OutboxStatus;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import java.util.UUID;

public interface OutboxEventRepository extends ReactiveCrudRepository<OutboxEvent, UUID> {

	Flux<OutboxEvent> findByStatus(OutboxStatus status);
}