package com.pd.ecommerce.security;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class JwtSecurityContextRepository implements ServerSecurityContextRepository {

	private final JwtAuthenticationManager manager;


	@Override
	public Mono<Void> save(ServerWebExchange exchange, SecurityContext context) {
		return Mono.empty();
	}

	@Override
	public Mono<SecurityContext> load(ServerWebExchange exchange) {
		String authHeader = exchange.getRequest()
			.getHeaders()
			.getFirst(HttpHeaders.AUTHORIZATION);

		if (authHeader == null || !authHeader.startsWith("Bearer ")) {
			return Mono.empty();
		}

		String token = authHeader.substring(7);
		Authentication auth = new UsernamePasswordAuthenticationToken(token, token);

		return manager.authenticate(auth)
			.map(SecurityContextImpl::new);
	}
}