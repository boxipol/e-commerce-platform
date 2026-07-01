package com.pd.ecommerce.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;
import java.net.InetSocketAddress;

@Configuration
public class RateLimitConfig {

	/**
	 * Authenticated requests are bucketed per user (X-User-Id set by JwtAuthenticationFilter).
	 * Public routes (login, register, webhooks) fall back to the client IP.
	 */
	@Bean
	public KeyResolver userOrIpKeyResolver() {
		return exchange -> {
			String userId = exchange.getRequest()
				.getHeaders()
				.getFirst("X-User-Id");

			if (userId != null && !userId.isBlank()) {
				return Mono.just("user:" + userId);
			}

			InetSocketAddress remote = exchange.getRequest()
				.getRemoteAddress();

			String ip = remote != null ? remote.getAddress().getHostAddress() : "unknown";

			return Mono.just("ip:" + ip);
		};
	}
}