package com.pd.ecommerce.integration;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

/**
 * Base class for order-service integration tests.
 *
 * <p>Starts a real PostgreSQL database via Testcontainers and binds the application's R2DBC
 * connection to it. The container is a shared static singleton, started once for the whole suite.
 * Requires a running Docker daemon.
 */
@Testcontainers
public abstract class AbstractPostgresIntegrationTest {

	static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>(DockerImageName.parse("postgres:16-alpine"))
		.withDatabaseName("orders_db")
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

		registry.add("spring.sql.init.mode", () -> "always");
		registry.add("spring.sql.init.schema-locations", () -> "classpath:schema-it.sql");
		registry.add("spring.sql.init.data-locations", () -> "");
		registry.add("spring.flyway.enabled", () -> "false");

		registry.add("spring.kafka.bootstrap-servers", () -> "localhost:0");
		registry.add("spring.autoconfigure.exclude",
			() -> "org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration");
	}
}