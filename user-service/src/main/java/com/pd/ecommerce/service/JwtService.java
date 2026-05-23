package com.pd.ecommerce.service;

import com.pd.ecommerce.entity.User;
import com.pd.ecommerce.security.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;

@Service
@RequiredArgsConstructor
public final class JwtService {

	private final JwtProperties properties;


	public String generateToken(User user) {
		Instant now = Instant.now();

		return Jwts.builder()
			.subject(user.getId().toString())
			.claim("email", user.getEmail())
			.claim("role", user.getRole().name())
			.issuedAt(Date.from(now))
			.expiration(Date.from(now.plusMillis(properties.getExpiration())))
			.signWith(getKey())
			.compact();
	}

	public String extractUserId(String token) {
		return extractClaims(token).getSubject();
	}

	public String extractRole(String token) {
		return extractClaims(token).get("role", String.class);
	}

	public boolean isValid(String token) {
		try {
			extractClaims(token);
			return true;
		} catch (Exception ex) {
			return false;
		}
	}

//	==================== PRIVATE ====================

	private Claims extractClaims(String token) {
		return Jwts.parser()
			.verifyWith(getKey())
			.build()
			.parseSignedClaims(token)
			.getPayload();
	}

	private SecretKey getKey() {
		byte[] keyBytes = Decoders.BASE64.decode(properties.getSecret());
		return Keys.hmacShaKeyFor(keyBytes);
	}
}