package com.pd.ecommerce.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Component
public class JwtService {

	@Value("${jwt.secret}")
	private String secret;


	private SecretKey getKey() {
		return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
	}

	public Claims extractClaims(String token) {
		return Jwts.parser()
			.verifyWith(getKey())
			.build()
			.parseSignedClaims(token)
			.getPayload();
	}

	public String extractUsername(String token) {
		return extractClaims(token).getSubject();
	}

	public boolean isTokenValid(String token) {
		try {
			extractClaims(token);
			return true;
		} catch (Exception ex) {
			return false;
		}
	}
}