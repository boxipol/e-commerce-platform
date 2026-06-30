package com.pd.ecommerce.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pd.ecommerce.config.R2dbcConfig;
import com.pd.ecommerce.entity.Payment;
import com.pd.ecommerce.entity.PaymentProvider;
import com.pd.ecommerce.entity.PaymentStatus;
import com.pd.ecommerce.event.OrderEventItem;
import com.pd.ecommerce.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import reactor.test.StepVerifier;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for {@link PaymentRepository} against a real PostgreSQL instance (Testcontainers).
 *
 * <p>Exercises the custom {@code List<OrderEventItem>} ↔ JSONB conversion (registered in
 * {@link R2dbcConfig}) and the hand-written {@code updateStatus} / {@code updateProviderData}
 * statements, including their affected-row-count return values.
 */
@DataR2dbcTest
@Import(R2dbcConfig.class)
@DisplayName("PaymentRepository Integration Tests")
class PaymentRepositoryIntegrationTest extends AbstractPostgresIntegrationTest {

	/** {@link R2dbcConfig} requires an {@link ObjectMapper} for the JSON converters. */
	@TestConfiguration
	static class JacksonTestConfig {

		@Bean
		@Primary
		ObjectMapper objectMapper() {
			return new ObjectMapper();
		}
	}

	@Autowired
	private PaymentRepository repository;


	@BeforeEach
	void setUp() {
		repository.deleteAll().block();
	}

	private Payment newPayment(PaymentStatus status) {
		Instant now = Instant.now();
		return Payment.builder()
			.orderId(UUID.randomUUID())
			.publicOrderId("ORD-1")
			.userId(UUID.randomUUID())
			.userMail("buyer@example.com")
			.amount(new BigDecimal("199.99"))
			.items(List.of(new OrderEventItem(UUID.randomUUID(), "SKU-1", 2)))
			.currency("USD")
			.status(status)
			.provider(PaymentProvider.STRIPE)
			.createdAt(now)
			.updatedAt(now)
			.build();
	}

	@Test
	@DisplayName("save - should persist a payment with its order items serialized to JSONB")
	void testSavePersistsItems() {
		Payment saved = repository.save(newPayment(PaymentStatus.PENDING)).block();

		StepVerifier.create(repository.findById(saved.getId()))
			.assertNext(found -> {
				assertThat(found.getStatus()).isEqualTo(PaymentStatus.PENDING);
				assertThat(found.getProvider()).isEqualTo(PaymentProvider.STRIPE);
				assertThat(found.getItems()).hasSize(1);
				assertThat(found.getItems().get(0).sku()).isEqualTo("SKU-1");
				assertThat(found.getItems().get(0).quantity()).isEqualTo(2);
			})
			.verifyComplete();
	}

	@Test
	@DisplayName("updateStatus - should change the status and report one row updated")
	void testUpdateStatus() {
		Payment saved = repository.save(newPayment(PaymentStatus.PENDING)).block();

		StepVerifier.create(repository.updateStatus(saved.getId(), PaymentStatus.COMPLETED, Instant.now()))
			.expectNext(1)
			.verifyComplete();

		StepVerifier.create(repository.findById(saved.getId()))
			.assertNext(found -> assertThat(found.getStatus()).isEqualTo(PaymentStatus.COMPLETED))
			.verifyComplete();
	}

	@Test
	@DisplayName("updateStatus - should report zero rows when the payment does not exist")
	void testUpdateStatusMissing() {
		StepVerifier.create(repository.updateStatus(UUID.randomUUID(), PaymentStatus.FAILED, Instant.now()))
			.expectNext(0)
			.verifyComplete();
	}

	@Test
	@DisplayName("findByOrderId - returns the payment matching the given order ID")
	void testFindByOrderId() {
		Payment saved = repository.save(newPayment(PaymentStatus.PENDING)).block();

		StepVerifier.create(repository.findByOrderId(saved.getOrderId()))
			.assertNext(found -> {
				assertThat(found.getId()).isEqualTo(saved.getId());
				assertThat(found.getOrderId()).isEqualTo(saved.getOrderId());
				assertThat(found.getStatus()).isEqualTo(PaymentStatus.PENDING);
			})
			.verifyComplete();
	}

	@Test
	@DisplayName("findByOrderId - returns empty when no payment exists for the order")
	void testFindByOrderIdMissing() {
		StepVerifier.create(repository.findByOrderId(UUID.randomUUID()))
			.verifyComplete();
	}

	@Test
	@DisplayName("updateProviderData - should store the provider id and payment url")
	void testUpdateProviderData() {
		Payment saved = repository.save(newPayment(PaymentStatus.PENDING)).block();

		StepVerifier.create(repository.updateProviderData(
				saved.getId(), "pi_12345", "https://pay.example/abc", Instant.now()))
			.expectNext(1)
			.verifyComplete();

		StepVerifier.create(repository.findById(saved.getId()))
			.assertNext(found -> {
				assertThat(found.getProviderPaymentId()).isEqualTo("pi_12345");
				assertThat(found.getPaymentUrl()).isEqualTo("https://pay.example/abc");
			})
			.verifyComplete();
	}
}
