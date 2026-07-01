package com.pd.ecommerce.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("HeaderSanitizingFilter Tests")
class HeaderSanitizingFilterTest {

	@Mock
	private GatewayFilterChain chain;

	@InjectMocks
	private HeaderSanitizingFilter filter;


	@Test
	@DisplayName("strips X-User-Id, X-User-Email and X-Role from incoming request")
	void stripsTrustedHeaders() {
		MockServerWebExchange exchange = MockServerWebExchange.from(
			MockServerHttpRequest.get("/api/v1/orders/123")
				.header("X-User-Id", "injected-id")
				.header("X-User-Email", "hacker@evil.com")
				.header("X-Role", "ADMIN")
				.header("Authorization", "Bearer some-token")
		);

		when(chain.filter(any())).thenReturn(Mono.empty());

		filter.filter(exchange, chain);

		ArgumentCaptor<ServerWebExchange> captor = ArgumentCaptor.forClass(ServerWebExchange.class);
		verify(chain).filter(captor.capture());

		ServerHttpRequest sanitized = captor.getValue().getRequest();
		assertThat(sanitized.getHeaders().getFirst("X-User-Id")).isNull();
		assertThat(sanitized.getHeaders().getFirst("X-User-Email")).isNull();
		assertThat(sanitized.getHeaders().getFirst("X-Role")).isNull();
		assertThat(sanitized.getHeaders().getFirst("Authorization")).isEqualTo("Bearer some-token");
	}

	@Test
	@DisplayName("passes through requests that have no trusted headers")
	void passesThroughCleanRequests() {
		MockServerWebExchange exchange = MockServerWebExchange.from(
			MockServerHttpRequest.get("/api/v1/products").header("Accept", "application/json")
		);

		when(chain.filter(any())).thenReturn(Mono.empty());

		filter.filter(exchange, chain);

		ArgumentCaptor<ServerWebExchange> captor = ArgumentCaptor.forClass(ServerWebExchange.class);
		verify(chain).filter(captor.capture());

		ServerHttpRequest sanitized = captor.getValue().getRequest();
		assertThat(sanitized.getHeaders().getFirst("Accept")).isEqualTo("application/json");
	}

	@Test
	@DisplayName("runs at HIGHEST_PRECEDENCE so it fires before JwtAuthenticationFilter")
	void hasHighestPrecedenceOrder() {
		assertThat(filter.getOrder()).isEqualTo(Integer.MIN_VALUE);
	}
}