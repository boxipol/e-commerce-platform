package com.pd.ecommerce.service;

import com.pd.ecommerce.config.JwtProperties;
import com.pd.ecommerce.entity.User;
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
			.claim("email", user.getEmail()) // todo remove and fetch from order
			.claim("role", user.getRole().name())
			.issuedAt(Date.from(now))
			.expiration(Date.from(now.plusMillis(properties.getExpiration())))
			.signWith(getKey())
			.compact();
	}

//	==================== PRIVATE ====================

	private SecretKey getKey() {
		byte[] keyBytes = Decoders.BASE64.decode(properties.readSecret());
		return Keys.hmacShaKeyFor(keyBytes);
	}
}