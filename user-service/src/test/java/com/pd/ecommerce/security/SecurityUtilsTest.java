package com.pd.ecommerce.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("SecurityUtils Tests")
class SecurityUtilsTest {

	@Test
	@DisplayName("getCurrentUserId - should return the UUID from the authentication principal")
	void testGetCurrentUserId() {
		UUID userId = UUID.randomUUID();
		var auth = new UsernamePasswordAuthenticationToken(userId.toString(), null, List.of());

		StepVerifier.create(
				SecurityUtils.getCurrentUserId()
					.contextWrite(ReactiveSecurityContextHolder.withAuthentication(auth)))
			.assertNext(id -> assertThat(id).isEqualTo(userId))
			.verifyComplete();
	}

	@Test
	@DisplayName("getCurrentUserId - should be empty when there is no security context")
	void testGetCurrentUserIdEmpty() {
		StepVerifier.create(SecurityUtils.getCurrentUserId())
			.verifyComplete();
	}
}