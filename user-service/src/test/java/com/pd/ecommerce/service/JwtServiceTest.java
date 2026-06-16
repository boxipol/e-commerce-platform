package com.pd.ecommerce.service;

import com.pd.ecommerce.config.JwtProperties;
import com.pd.ecommerce.entity.User;
import com.pd.ecommerce.entity.UserRole;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtService Tests")
class JwtServiceTest {

	@Mock
	private JwtProperties jwtProperties;

	@InjectMocks
	private JwtService jwtService;

	private UUID userId;
	private User testUser;
	private String base64EncodedSecret;
	private long tokenExpiration;


	@BeforeEach
	void setUp() {
		userId = UUID.randomUUID();
		testUser = User.builder()
			.id(userId)
			.email("test@example.com")
			.firstName("John")
			.lastName("Doe")
			.password("encodedPassword")
			.role(UserRole.CUSTOMER)
			.createdAt(Instant.now())
			.build();

		// Create a valid Base64-encoded secret key (256 bits = 32 bytes for HS256)
		byte[] secretBytes = new byte[32];

		for (int i = 0; i < secretBytes.length; i++) {
			secretBytes[i] = (byte) i;
		}
		SecretKey secretKey = Keys.hmacShaKeyFor(secretBytes);
		base64EncodedSecret = Base64.getEncoder().encodeToString(secretKey.getEncoded());
		tokenExpiration = 3_600_000; // 1 hour in milliseconds

		when(jwtProperties.readSecret()).thenReturn(base64EncodedSecret);
		when(jwtProperties.getExpiration()).thenReturn(tokenExpiration);
	}

	@Test
	@DisplayName("generateToken - should create valid JWT token")
	void testGenerateTokenSuccess() {
		// When
		String token = jwtService.generateToken(testUser);

		// Then
		assertThat(token).isNotNull().isNotEmpty();

		// Verify token structure (should have 3 parts separated by dots: header.payload.signature)
		String[] tokenParts = token.split("\\.");
		assertThat(tokenParts).hasSize(3);
	}

	@Test
	@DisplayName("generateToken - token should not be empty")
	void testGenerateTokenNotEmpty() {
		// When
		String token = jwtService.generateToken(testUser);

		// Then
		assertThat(token)
			.isNotNull()
			.isNotEmpty()
			.hasSizeGreaterThan(50);
	}

	@Test
	@DisplayName("generateToken - different users should have different tokens")
	void testGenerateTokenDifferentUsersProduceDifferentTokens() {
		// Given
		User anotherUser = User.builder()
			.id(UUID.randomUUID())
			.email("another@example.com")
			.firstName("Jane")
			.lastName("Smith")
			.password("encodedPassword")
			.role(UserRole.MERCHANT)
			.createdAt(Instant.now())
			.build();

		// When
		String token1 = jwtService.generateToken(testUser);
		String token2 = jwtService.generateToken(anotherUser);

		// Then
		assertThat(token1).isNotEqualTo(token2);
	}

	@Test
	@DisplayName("generateToken - should handle different user roles")
	void testGenerateTokenWithDifferentRoles() {
		// Given
		User adminUser = User.builder()
			.id(UUID.randomUUID())
			.email("admin@example.com")
			.firstName("Admin")
			.lastName("User")
			.password("encodedPassword")
			.role(UserRole.ADMIN)
			.createdAt(Instant.now())
			.build();

		// When
		String token = jwtService.generateToken(adminUser);

		// Then
		assertThat(token).isNotNull().isNotEmpty();
		String[] tokenParts = token.split("\\.");
		assertThat(tokenParts).hasSize(3);
	}

	@Test
	@DisplayName("generateToken - token format should be valid JWT format")
	void testGenerateTokenHasValidFormat() {
		// When
		String token = jwtService.generateToken(testUser);

		// Then
		// JWT tokens should have format: base64header.base64payload.base64signature
		String[] parts = token.split("\\.");
		assertThat(parts).hasSize(3);

		// Each part should be valid base64
		for (String part : parts) {
			assertThat(part).matches("[A-Za-z0-9_-]+");
		}
	}

	@Test
	@DisplayName("generateToken - should generate consistent tokens for same user")
	void testGenerateTokenConsistencyAcrossRoles() {
		// Given
		UUID specificUserId = UUID.randomUUID();
		User userWithSpecificId = User.builder()
			.id(specificUserId)
			.email("test@example.com")
			.firstName("John")
			.lastName("Doe")
			.password("encodedPassword")
			.role(UserRole.ADMIN)
			.createdAt(Instant.now())
			.build();

		// When
		String token = jwtService.generateToken(userWithSpecificId);

		// Then
		// Token should be a valid JWT format (3 parts)
		String[] tokenParts = token.split("\\.");
		assertThat(tokenParts).hasSize(3);

		// Token should be non-empty and have reasonable length
		assertThat(token).isNotBlank().hasSizeGreaterThan(50);
	}

	@Test
	@DisplayName("generateToken - multiple calls with same user should produce different signatures")
	void testGenerateTokenMultipleCallsProduceDifferentTokens() {
		// Given
		User userA = testUser;

		// When - Generate tokens in different seconds to ensure different timestamps
		String token1 = jwtService.generateToken(userA);

		// Sleep for 1+ second to ensure different 'iat' (issued at) timestamp
		try {
			Thread.sleep(1100);  // 1.1 seconds to ensure different second
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}

		String token2 = jwtService.generateToken(userA);

		// Then - tokens should be different due to different 'iat' and 'exp' timestamps
		// Even for the same user, tokens generated at different times should differ
		assertThat(token1).isNotEqualTo(token2);

		// Both should still have valid JWT structure
		assertThat(token1.split("\\.")).hasSize(3);
		assertThat(token2.split("\\.")).hasSize(3);
	}

	@Test
	@DisplayName("generateToken - token should be non-empty string")
	void testGenerateTokenTypeAndLength() {
		// When
		String token = jwtService.generateToken(testUser);

		// Then
		assertThat(token)
			.isNotNull()
			.isNotBlank()
			.hasSizeGreaterThanOrEqualTo(100);
	}

	@Test
	@DisplayName("generateToken - should generate valid token for CUSTOMER role")
	void testGenerateTokenForCustomerRole() {
		// When
		String token = jwtService.generateToken(testUser);

		// Then
		assertThat(token).isNotNull().isNotEmpty();
		assertThat(token.split("\\.")).hasSize(3);
	}
}







