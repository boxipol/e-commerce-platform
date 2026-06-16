package com.pd.ecommerce.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Gateway JwtProperties Tests")
class JwtPropertiesTest {

	@Test
	@DisplayName("readSecret - should read and trim the secret file content")
	void testReadSecretSuccess(@TempDir Path tempDir) throws IOException {
		Path secretFile = tempDir.resolve("jwt-secret.txt");
		Files.writeString(secretFile, "  my-super-secret-value  \n");

		JwtProperties properties = new JwtProperties();
		properties.setSecretFile(secretFile.toString());

		assertThat(properties.readSecret()).isEqualTo("my-super-secret-value");
	}

	@Test
	@DisplayName("readSecret - should throw when secret file is missing")
	void testReadSecretMissingFile() {
		JwtProperties properties = new JwtProperties();
		properties.setSecretFile("/non/existent/path/secret.txt");

		assertThatThrownBy(properties::readSecret)
			.isInstanceOf(RuntimeException.class)
			.hasMessageContaining("Failed to read JWT secret");
	}

	@Test
	@DisplayName("getExpiration - should return configured expiration")
	void testExpirationAccessor() {
		JwtProperties properties = new JwtProperties();
		properties.setExpiration(3_600_000);

		assertThat(properties.getExpiration()).isEqualTo(3_600_000);
	}
}