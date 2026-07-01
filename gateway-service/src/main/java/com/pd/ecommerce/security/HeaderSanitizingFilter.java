package com.pd.ecommerce.security;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import java.util.List;

@Component
public class HeaderSanitizingFilter implements GlobalFilter, Ordered {

	private static final List<String> TRUSTED_HEADERS = List.of("X-User-Id", "X-User-Email", "X-Role");


	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
		ServerHttpRequest sanitized = exchange.getRequest()
			.mutate()
			.headers(h -> TRUSTED_HEADERS.forEach(h::remove))
			.build();

		return chain.filter(exchange.mutate().request(sanitized).build());
	}

	@Override
	public int getOrder() {
		return Ordered.HIGHEST_PRECEDENCE;
	}
}