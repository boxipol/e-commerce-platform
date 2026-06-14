package com.pd.ecommerce.dto;

import com.pd.ecommerce.entity.UserRole;
import lombok.Builder;

@Builder
public record UserProfileResponse(
	String email,
	String firstName,
	String lastName,
	UserRole role
) {}