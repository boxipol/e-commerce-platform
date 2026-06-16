package com.pd.ecommerce.integration;

import com.pd.ecommerce.repository.InventoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for {@link InventoryRepository} running against a real PostgreSQL instance
 * (Testcontainers). Verifies the hand-written SQL queries, including the concurrency-safe
 * {@code decreaseStock} statement that guards against overselling.
 */
@DataR2dbcTest
@DisplayName("InventoryRepository Integration Tests")
class InventoryRepositoryIntegrationTest extends AbstractPostgresIntegrationTest {

	@Autowired
	private InventoryRepository repository;

	private UUID productId;


	@BeforeEach
	void setUp() {
		productId = UUID.randomUUID();
		Instant now = Instant.now();

		// Clean slate then seed a known stock level.
		repository.deleteAll()
			.then(repository.insert(productId, 10, now, now))
			.block();
	}

	@Test
	@DisplayName("insert - should persist a new inventory row and return it")
	void testInsert() {
		UUID newProduct = UUID.randomUUID();
		Instant now = Instant.now();

		StepVerifier.create(repository.insert(newProduct, 42, now, now))
			.assertNext(inv -> {
				assertThat(inv.getProductId()).isEqualTo(newProduct);
				assertThat(inv.getQuantity()).isEqualTo(42);
			})
			.verifyComplete();

		StepVerifier.create(repository.findById(newProduct))
			.assertNext(inv -> assertThat(inv.getQuantity()).isEqualTo(42))
			.verifyComplete();
	}

	@Test
	@DisplayName("decreaseStock - should reduce quantity and return 1 when stock is sufficient")
	void testDecreaseStockSufficient() {
		StepVerifier.create(repository.decreaseStock(productId, 4, Instant.now()))
			.expectNext(1)
			.verifyComplete();

		StepVerifier.create(repository.findById(productId))
			.assertNext(inv -> assertThat(inv.getQuantity()).isEqualTo(6))
			.verifyComplete();
	}

	@Test
	@DisplayName("decreaseStock - should not change quantity and return 0 when stock is insufficient")
	void testDecreaseStockInsufficient() {
		StepVerifier.create(repository.decreaseStock(productId, 50, Instant.now()))
			.expectNext(0)
			.verifyComplete();

		// Quantity must remain untouched to avoid overselling.
		StepVerifier.create(repository.findById(productId))
			.assertNext(inv -> assertThat(inv.getQuantity()).isEqualTo(10))
			.verifyComplete();
	}

	@Test
	@DisplayName("decreaseStock - should allow draining stock to exactly zero")
	void testDecreaseStockToZero() {
		StepVerifier.create(repository.decreaseStock(productId, 10, Instant.now()))
			.expectNext(1)
			.verifyComplete();

		StepVerifier.create(repository.findById(productId))
			.assertNext(inv -> assertThat(inv.getQuantity()).isZero())
			.verifyComplete();
	}

	@Test
	@DisplayName("decreaseStock - concurrent reservations must never oversell")
	void testDecreaseStockConcurrencySafe() {
		// Two parallel attempts to take 7 from a stock of 10: exactly one should win.
		Mono<Integer> first = repository.decreaseStock(productId, 7, Instant.now()).subscribeOn(reactor.core.scheduler.Schedulers.parallel());
		Mono<Integer> second = repository.decreaseStock(productId, 7, Instant.now()).subscribeOn(reactor.core.scheduler.Schedulers.parallel());
		StepVerifier.create(Mono.zip(first, second)).assertNext(results -> {
			int successes = results.getT1() + results.getT2();
			assertThat(successes).isEqualTo(1);
		}).verifyComplete();
		StepVerifier.create(repository.findById(productId)).assertNext(inv -> assertThat(inv.getQuantity()).isEqualTo(3)).verifyComplete();
	}

	@Test
	@DisplayName("update - should overwrite the absolute quantity")
	void testUpdate() {
		StepVerifier.create(repository.update(productId, 99, Instant.now()))
			.assertNext(inv -> assertThat(inv.getQuantity()).isEqualTo(99))
			.verifyComplete();

		StepVerifier.create(repository.findById(productId))
			.assertNext(inv -> assertThat(inv.getQuantity()).isEqualTo(99))
			.verifyComplete();
	}

	@Test
	@DisplayName("delete - should remove the inventory row")
	void testDelete() {
		StepVerifier.create(
				repository.findById(productId)
					.flatMap(repository::delete)
					.then(repository.findById(productId)))
			.verifyComplete();
	}
}