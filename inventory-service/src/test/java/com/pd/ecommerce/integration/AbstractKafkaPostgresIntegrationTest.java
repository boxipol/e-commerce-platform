package com.pd.ecommerce.integration;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

/**
 * Base class for full-stack inventory-service integration tests that need both a real PostgreSQL
 * database and a real Kafka broker (Testcontainers).
 *
 * <p>Used to verify the inventory slice of the order → payment → inventory choreography: the service
 * consumes {@code payment.completed}, reserves stock in PostgreSQL, and emits either
 * {@code reservation.completed} or {@code reservation.failed} back onto Kafka.
 *
 * <p>Both containers are shared static singletons started once for the suite. Requires a running
 * Docker daemon.
 */
@Testcontainers
public abstract class AbstractKafkaPostgresIntegrationTest {

	static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>(DockerImageName.parse("postgres:16-alpine"))
		.withDatabaseName("inventory_db")
		.withUsername("ecommerce_user")
		.withPassword("ecommerce_pass");

	static final KafkaContainer KAFKA = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.6.1"));

	static {
		POSTGRES.start();
		KAFKA.start();
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

		registry.add("spring.kafka.bootstrap-servers", KAFKA::getBootstrapServers);
	}
}