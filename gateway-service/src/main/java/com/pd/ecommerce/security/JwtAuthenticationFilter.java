package com.pd.ecommerce.security;

import com.pd.ecommerce.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public final class JwtAuthenticationFilter implements GlobalFilter {

	private final JwtService jwtService;


	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
		String path = exchange.getRequest().getURI().getPath();

		if (path.startsWith("/api/v1/users") || path.startsWith("/api/v1/payments/webhooks")
				|| path.startsWith("/swagger-ui") || path.startsWith("/v3/api-docs")
				|| path.endsWith("/v3/api-docs")) {
			return chain.filter(exchange);
		}

		String authHeader = exchange.getRequest()
			.getHeaders()
			.getFirst(HttpHeaders.AUTHORIZATION);

		if (authHeader == null || !authHeader.startsWith("Bearer ")) {
			exchange.getResponse()
				.setStatusCode(HttpStatus.UNAUTHORIZED);

			return exchange.getResponse()
				.setComplete();
		}

		String token = authHeader.substring(7);

		if (!jwtService.isTokenValid(token)) {
			exchange.getResponse()
				.setStatusCode(HttpStatus.UNAUTHORIZED);

			return exchange.getResponse().setComplete();
		}

		String userId = jwtService.extractUserId(token);
		String userMail = jwtService.extractUserMail(token);
		String role = jwtService.extractRole(token);

		ServerHttpRequest mutatedRequest = exchange.getRequest()
			.mutate()
			.header("X-User-Id", userId)
			.header("X-User-Email", userMail)
			.header("X-Role", role)
			.build();

		return chain.filter(
			exchange.mutate()
				.request(mutatedRequest)
				.build()
		);
	}
}