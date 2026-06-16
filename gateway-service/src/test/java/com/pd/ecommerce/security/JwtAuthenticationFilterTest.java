package com.pd.ecommerce.security;

import com.pd.ecommerce.service.JwtService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtAuthenticationFilter Tests")
class JwtAuthenticationFilterTest {

	@Mock
	private JwtService jwtService;

	@Mock
	private GatewayFilterChain chain;

	@InjectMocks
	private JwtAuthenticationFilter filter;


	@Test
	@DisplayName("filter - should skip auth for the users endpoint")
	void testSkipUsersEndpoint() {
		MockServerWebExchange exchange = MockServerWebExchange.from(
			MockServerHttpRequest.get("/api/v1/users/login"));
		when(chain.filter(exchange)).thenReturn(Mono.empty());

		StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();

		verify(chain).filter(exchange);
		verify(jwtService, never()).isTokenValid(any());
	}

	@Test
	@DisplayName("filter - should skip auth for the payment webhooks endpoint")
	void testSkipWebhooksEndpoint() {
		MockServerWebExchange exchange = MockServerWebExchange.from(
			MockServerHttpRequest.post("/api/v1/payments/webhooks/stripe"));
		when(chain.filter(exchange)).thenReturn(Mono.empty());

		StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();

		verify(chain).filter(exchange);
		verify(jwtService, never()).isTokenValid(any());
	}

	@Test
	@DisplayName("filter - should return 401 when authorization header is missing")
	void testMissingAuthHeader() {
		MockServerWebExchange exchange = MockServerWebExchange.from(
			MockServerHttpRequest.get("/api/v1/orders"));

		StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();

		assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
		verify(chain, never()).filter(any());
	}

	@Test
	@DisplayName("filter - should return 401 when header does not start with Bearer")
	void testNonBearerAuthHeader() {
		MockServerWebExchange exchange = MockServerWebExchange.from(
			MockServerHttpRequest.get("/api/v1/orders")
				.header(HttpHeaders.AUTHORIZATION, "Basic abc123"));

		StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();

		assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
		verify(chain, never()).filter(any());
	}

	@Test
	@DisplayName("filter - should return 401 when token is invalid")
	void testInvalidToken() {
		MockServerWebExchange exchange = MockServerWebExchange.from(
			MockServerHttpRequest.get("/api/v1/orders")
				.header(HttpHeaders.AUTHORIZATION, "Bearer invalid-token"));
		when(jwtService.isTokenValid("invalid-token")).thenReturn(false);

		StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();

		assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
		verify(chain, never()).filter(any());
	}

	@Test
	@DisplayName("filter - should forward request with user headers when token is valid")
	void testValidTokenForwardsWithHeaders() {
		MockServerWebExchange exchange = MockServerWebExchange.from(
			MockServerHttpRequest.get("/api/v1/orders")
				.header(HttpHeaders.AUTHORIZATION, "Bearer valid-token"));

		when(jwtService.isTokenValid("valid-token")).thenReturn(true);
		when(jwtService.extractUserId("valid-token")).thenReturn("user-123");
		when(jwtService.extractUserMail("valid-token")).thenReturn("test@example.com");
		when(jwtService.extractRole("valid-token")).thenReturn("CUSTOMER");
		when(chain.filter(any())).thenReturn(Mono.empty());

		StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();

		ArgumentCaptor<ServerWebExchange> exchangeCaptor = ArgumentCaptor.forClass(ServerWebExchange.class);
		verify(chain).filter(exchangeCaptor.capture());

		ServerHttpRequest forwarded = exchangeCaptor.getValue().getRequest();
		assertThat(forwarded.getHeaders().getFirst("X-User-Id")).isEqualTo("user-123");
		assertThat(forwarded.getHeaders().getFirst("X-User-Email")).isEqualTo("test@example.com");
		assertThat(forwarded.getHeaders().getFirst("X-Role")).isEqualTo("CUSTOMER");
	}
}