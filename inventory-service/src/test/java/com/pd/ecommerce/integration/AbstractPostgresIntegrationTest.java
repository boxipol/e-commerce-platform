package com.pd.ecommerce.integration;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

/**
 * Base class for inventory-service integration tests.
 *
 * <p>Starts a real PostgreSQL database in a Testcontainers-managed Docker container and points the
 * application's R2DBC connection at it. The container is started once and shared across all
 * subclasses (static singleton pattern) to keep the suite fast.
 *
 * <p>Requires a running Docker daemon. When Docker is unavailable the tests that extend this class
 * are skipped rather than failing the build (see {@code @EnabledIf} usage on the concrete tests).
 */
@Testcontainers
public abstract class AbstractPostgresIntegrationTest {

	static final PostgreSQLContainer<?> POSTGRES =
		new PostgreSQLContainer<>(DockerImageName.parse("postgres:16-alpine"))
			.withDatabaseName("inventory_db")
			.withUsername("ecommerce_user")
			.withPassword("ecommerce_pass");

	static {
		POSTGRES.start();
	}

	@DynamicPropertySource
	static void registerProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.r2dbc.url", () -> "r2dbc:postgresql://%s:%d/%s".formatted(
			POSTGRES.getHost(),
			POSTGRES.getFirstMappedPort(),
			POSTGRES.getDatabaseName()));
		registry.add("spring.r2dbc.username", POSTGRES::getUsername);
		registry.add("spring.r2dbc.password", POSTGRES::getPassword);

		// Apply our test schema on startup; disable the production seed script.
		registry.add("spring.sql.init.mode", () -> "always");
		registry.add("spring.sql.init.schema-locations", () -> "classpath:schema-it.sql");
		registry.add("spring.sql.init.data-locations", () -> "");

		// Avoid needing a real Kafka broker for repository-level tests.
		registry.add("spring.kafka.bootstrap-servers", () -> "localhost:0");
		registry.add("spring.autoconfigure.exclude",
			() -> "org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration");
	}
}
