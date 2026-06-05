package com.pd.ecommerce.repository;

import com.pd.ecommerce.entity.Order;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;
import java.time.Instant;
import java.util.UUID;

public interface OrderRepository extends ReactiveCrudRepository<Order, UUID> {

	@Query("""
		    UPDATE orders
		    SET status = 'PAID',
		        updated_at = :updatedAt
		    WHERE id = :orderId
		      AND status = 'CREATED'
		""")
	Mono<Integer> markAsPaid(UUID orderId, Instant updatedAt);
}