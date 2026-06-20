package com.pd.ecommerce.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Gateway JwtProperties Tests")
class JwtPropertiesTest {

	@Test
	@DisplayName("getSecret - should return configured secret")
	void testSecretAccessor() {
		JwtProperties properties = new JwtProperties();
		properties.setSecret("my-super-secret");

		assertThat(properties.getSecret()).isEqualTo("my-super-secret");
	}

	@Test
	@DisplayName("getExpiration - should return configured expiration")
	void testExpirationAccessor() {
		JwtProperties properties = new JwtProperties();
		properties.setExpiration(3_600_000);

		assertThat(properties.getExpiration()).isEqualTo(3_600_000);
	}
}