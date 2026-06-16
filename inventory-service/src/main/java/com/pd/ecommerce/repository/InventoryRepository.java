package com.pd.ecommerce.repository;

import com.pd.ecommerce.entity.Inventory;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;
import java.time.Instant;
import java.util.UUID;

public interface InventoryRepository extends ReactiveCrudRepository<Inventory, UUID> {

	@Modifying
	@Query("""
		UPDATE inventory
		SET quantity = quantity - :quantity,
		    updated_at = :updatedAt
		WHERE product_id = :productId
		  AND quantity >= :quantity
		""")
	Mono<Integer> decreaseStock(UUID productId, Integer quantity, Instant updatedAt);

	@Query("""
		INSERT INTO inventory (product_id, quantity, created_at, updated_at)
		VALUES (:productId, :quantity, :createdAt, :updatedAt)
		RETURNING product_id, quantity, created_at, updated_at
		""")
	Mono<Inventory> insert(UUID productId, Integer quantity, Instant createdAt, Instant updatedAt);

	@Query("""
		UPDATE inventory
		SET quantity = :quantity,
		    updated_at = :updatedAt
		WHERE product_id = :productId
		RETURNING product_id, quantity, created_at, updated_at
		""")
	Mono<Inventory> update(UUID productId, Integer quantity, Instant updatedAt);
}