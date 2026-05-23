package com.pd.ecommerce.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import reactor.core.publisher.Mono;
import java.util.UUID;

public final class SecurityUtils {

	public static Mono<UUID> getCurrentUserId() {
		return ReactiveSecurityContextHolder.getContext()
			.map(SecurityContext::getAuthentication)
			.map(Authentication::getPrincipal)
			.cast(String.class)
			.map(UUID::fromString);
	}
}