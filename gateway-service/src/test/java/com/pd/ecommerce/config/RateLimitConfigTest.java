package com.pd.ecommerce.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.test.StepVerifier;

@DisplayName("RateLimitConfig")
class RateLimitConfigTest {

	private KeyResolver resolver;


	@BeforeEach
	void setUp() {
		resolver = new RateLimitConfig().userOrIpKeyResolver();
	}

	@Test
	@DisplayName("resolves to user:<id> when X-User-Id header is present")
	void resolvesUserKey() {
		MockServerHttpRequest request = MockServerHttpRequest
			.get("/api/v1/orders")
			.header("X-User-Id", "user-abc-123")
			.build();

		StepVerifier.create(resolver.resolve(MockServerWebExchange.from(request)))
			.expectNext("user:user-abc-123")
			.verifyComplete();
	}

	@Test
	@DisplayName("resolves to ip:<address> when X-User-Id header is absent")
	void resolvesIpKeyWhenNoUserId() {
		MockServerHttpRequest request = MockServerHttpRequest
			.get("/api/v1/users/login")
			.remoteAddress(new java.net.InetSocketAddress("192.168.1.42", 4567))
			.build();

		StepVerifier.create(resolver.resolve(MockServerWebExchange.from(request)))
			.expectNext("ip:192.168.1.42")
			.verifyComplete();
	}

	@Test
	@DisplayName("resolves to ip:<address> when X-User-Id header is blank")
	void resolvesIpKeyWhenUserIdBlank() {
		MockServerHttpRequest request = MockServerHttpRequest
			.get("/api/v1/products")
			.header("X-User-Id", "   ")
			.remoteAddress(new java.net.InetSocketAddress("10.0.0.1", 1234))
			.build();

		StepVerifier.create(resolver.resolve(MockServerWebExchange.from(request)))
			.expectNext("ip:10.0.0.1")
			.verifyComplete();
	}
}