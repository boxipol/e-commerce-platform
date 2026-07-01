package com.pd.ecommerce;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.core.publisher.Mono;
import reactor.netty.DisposableServer;
import reactor.netty.http.server.HttpServer;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("ratelimit-it")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Rate Limit Integration Tests")
class RateLimitIntegrationTest {

	@Container
	static final GenericContainer<?> REDIS = new GenericContainer<>("redis:7")
		.withExposedPorts(6379)
		.waitingFor(Wait.forListeningPort());

	// Must be initialized before @DynamicPropertySource fires (during class loading)
	static final DisposableServer ECHO_SERVER = HttpServer.create()
		.port(0)
		.handle((req, res) -> res.status(200).sendString(Mono.just("ok")))
		.bindNow();

	@Autowired
	private WebTestClient webTestClient;

	@DynamicPropertySource
	static void overrideProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.data.redis.host", REDIS::getHost);
		registry.add("spring.data.redis.port", () -> REDIS.getMappedPort(6379));
		registry.add("test.backend.uri", () -> "http://127.0.0.1:" + ECHO_SERVER.port());
	}

	@Test
	@Order(1)
	@DisplayName("requests within burst capacity (3) are forwarded with 200")
	void requestsWithinBurstCapacitySucceed() {
		for (int i = 0; i < 3; i++) {
			webTestClient.get()
				.uri("/api/v1/users/ping")
				.exchange()
				.expectStatus().isOk();
		}
	}

	@Test
	@Order(2)
	@DisplayName("flooding beyond burst capacity yields 429 Too Many Requests")
	void floodingBeyondBurstReturns429() {
		// Burst was fully exhausted in test 1; replenishRate=1 token/sec means
		// less than 1 token has refilled in the few ms between tests.
		// Sending 15 rapid requests: almost all should be 429.
		List<Integer> statuses = new ArrayList<>();

		for (int i = 0; i < 15; i++) {
			int status = webTestClient.get()
				.uri("/api/v1/users/ping")
				.exchange()
				.returnResult(String.class)
				.getStatus()
				.value();
			statuses.add(status);
		}

		assertThat(statuses).as("at least one request must be rate-limited (429)").contains(429);
	}

	@AfterAll
	static void tearDown() {
		ECHO_SERVER.disposeNow();
	}
}