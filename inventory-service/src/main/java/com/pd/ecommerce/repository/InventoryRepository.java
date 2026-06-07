package com.pd.ecommerce.repository;

import com.pd.ecommerce.entity.Inventory;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;
import java.time.Instant;
import java.util.UUID;

public interface InventoryRepository extends ReactiveCrudRepository<Inventory, UUID> {

	@Query("""
		UPDATE inventory
		SET quantity = quantity - :quantity,
		    updated_at = :updatedAt
		WHERE product_id = :productId
		  AND quantity >= :quantity
		""")
	Mono<Integer> decreaseStock(UUID productId, Integer quantity, Instant updatedAt);
}