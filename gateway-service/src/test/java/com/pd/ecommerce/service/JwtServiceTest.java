package com.pd.ecommerce.service;

import com.pd.ecommerce.config.JwtProperties;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
@DisplayName("Gateway JwtService Tests")
class JwtServiceTest {

	@Mock
	private JwtProperties jwtProperties;

	@InjectMocks
	private JwtService jwtService;

	private SecretKey secretKey;
	private String base64EncodedSecret;


	@BeforeEach
	void setUp() {
		byte[] secretBytes = new byte[32];

		for (int i = 0; i < secretBytes.length; i++) {
			secretBytes[i] = (byte) i;
		}

		secretKey = Keys.hmacShaKeyFor(secretBytes);
		base64EncodedSecret = Base64.getEncoder().encodeToString(secretKey.getEncoded());

		lenient().when(jwtProperties.getSecret()).thenReturn(base64EncodedSecret);
	}

	private String buildToken(String subject, String email, String role, long ttlMillis) {
		Date now = new Date();

		return Jwts.builder()
			.subject(subject)
			.claim("email", email)
			.claim("role", role)
			.issuedAt(now)
			.expiration(new Date(now.getTime() + ttlMillis))
			.signWith(secretKey)
			.compact();
	}

	@Test
	@DisplayName("extractUserId - should return subject from token")
	void testExtractUserId() {
		String token = buildToken("user-123", "test@example.com", "CUSTOMER", 3_600_000);

		assertThat(jwtService.extractUserId(token)).isEqualTo("user-123");
	}

	@Test
	@DisplayName("extractUserMail - should return email claim from token")
	void testExtractUserMail() {
		String token = buildToken("user-123", "test@example.com", "CUSTOMER", 3_600_000);

		assertThat(jwtService.extractUserMail(token)).isEqualTo("test@example.com");
	}

	@Test
	@DisplayName("extractRole - should return role claim from token")
	void testExtractRole() {
		String token = buildToken("user-123", "test@example.com", "ADMIN", 3_600_000);

		assertThat(jwtService.extractRole(token)).isEqualTo("ADMIN");
	}

	@Test
	@DisplayName("isTokenValid - should return true for a valid token")
	void testIsTokenValidTrue() {
		String token = buildToken("user-123", "test@example.com", "CUSTOMER", 3_600_000);

		assertThat(jwtService.isTokenValid(token)).isTrue();
	}

	@Test
	@DisplayName("isTokenValid - should return false for an expired token")
	void testIsTokenValidExpired() {
		String token = buildToken("user-123", "test@example.com", "CUSTOMER", -1_000);

		assertThat(jwtService.isTokenValid(token)).isFalse();
	}

	@Test
	@DisplayName("isTokenValid - should return false for a malformed token")
	void testIsTokenValidMalformed() {
		assertThat(jwtService.isTokenValid("not-a-real-token")).isFalse();
	}

	@Test
	@DisplayName("isTokenValid - should return false for a token signed with a different key")
	void testIsTokenValidWrongSignature() {
		byte[] otherBytes = new byte[32];

		for (int i = 0; i < otherBytes.length; i++) {
			otherBytes[i] = (byte) (i + 100);
		}

		SecretKey otherKey = Keys.hmacShaKeyFor(otherBytes);
		String token = Jwts.builder()
			.subject("user-123")
			.expiration(new Date(System.currentTimeMillis() + 3_600_000))
			.signWith(otherKey)
			.compact();

		assertThat(jwtService.isTokenValid(token)).isFalse();
	}
}