package com.pd.ecommerce.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
@RequiredArgsConstructor
public class SecurityConfig {

	private final JwtSecurityContextRepository repository;


	@Bean
	public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
		return http
			.csrf(ServerHttpSecurity.CsrfSpec::disable)
			.httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
			.formLogin(ServerHttpSecurity.FormLoginSpec::disable)
			.securityContextRepository(repository)
			.authorizeExchange(exchange -> exchange
				.pathMatchers(
					"/api/v1/users/**"
				).permitAll()
				.pathMatchers("/admin/**")
				.hasRole("ADMIN")
				.anyExchange()
				.authenticated()
			)
			.build();
	}
}