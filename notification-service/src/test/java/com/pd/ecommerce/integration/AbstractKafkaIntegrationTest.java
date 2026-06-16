package com.pd.ecommerce.integration;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

/**
 * Base class for notification-service integration tests that need a real Kafka broker
 * (Testcontainers).
 *
 * <p>The notification service is a pure event consumer: it listens for user / order / payment
 * events and turns them into emails. These tests publish events onto Kafka and assert the service
 * reacts. The broker is a shared static singleton started once for the suite. Requires a running
 * Docker daemon.
 */
@Testcontainers
public abstract class AbstractKafkaIntegrationTest {

	static final KafkaContainer KAFKA = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.6.1"));

	static {
		KAFKA.start();
	}

	@DynamicPropertySource
	static void registerProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.kafka.bootstrap-servers", KAFKA::getBootstrapServers);

		// Avoid touching real SMTP and the host/port from application.yaml in tests.
		registry.add("spring.mail.host", () -> "localhost");
		registry.add("spring.mail.port", () -> 3025);
		registry.add("app.mail.from", () -> "noreply@example.com");
		// The mocked JavaMailSender confuses the actuator mail health contributor; disable it.
		registry.add("management.health.mail.enabled", () -> "false");
	}
}
