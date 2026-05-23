package com.pd.ecommerce.dto;

import lombok.Builder;

@Builder
public record AuthResponse(
	String accessToken,
	String tokenType,
	Long expiresIn
) {}