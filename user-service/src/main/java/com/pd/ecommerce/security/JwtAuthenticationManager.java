package com.pd.ecommerce.security;

import com.pd.ecommerce.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import java.util.Collection;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationManager implements ReactiveAuthenticationManager {

	private final JwtService jwtService;


	@Override
	public Mono<Authentication> authenticate(Authentication authentication) {
		String token = authentication.getCredentials()
			.toString();

		if (!jwtService.isValid(token)) {
			return Mono.empty();
		}

		String userId = jwtService.extractUserId(token);
		String role = jwtService.extractRole(token);

		Collection<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));

		Authentication auth = new UsernamePasswordAuthenticationToken(
			userId,
			null,
			authorities
		);

		return Mono.just(auth);
	}
}