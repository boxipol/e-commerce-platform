package com.pd.ecommerce.config;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
@Configuration
@ConfigurationProperties(prefix = "jwt")
@Getter
@Setter
public class JwtProperties {

	private String secretFile;
	private long expiration;


	public String readSecret() {
		try {
			return Files.readString(Path.of(secretFile)).trim();
		} catch (IOException e) {
			throw new RuntimeException("Failed to read JWT secret", e);
		}
	}
}