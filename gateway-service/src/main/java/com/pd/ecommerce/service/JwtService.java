package com.pd.ecommerce.service;

import com.pd.ecommerce.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import javax.crypto.SecretKey;

@Service
@RequiredArgsConstructor
public final class JwtService {

	private final JwtProperties jwtProperties;


	public String extractUserId(String token) {
		return extractClaims(token).getSubject();
	}

	public String extractUserMail(String token) {
		return extractClaims(token).get("email", String.class);
	}

	public String extractRole(String token) {
		return extractClaims(token).get("role", String.class);
	}

	public boolean isTokenValid(String token) {
		try {
			Jwts.parser()
				.verifyWith(getKey())
				.build()
				.parseSignedClaims(token);

			return true;
		} catch (Exception e) {
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
		byte[] keyBytes = Decoders.BASE64.decode(jwtProperties.getSecret());
		return Keys.hmacShaKeyFor(keyBytes);
	}
}