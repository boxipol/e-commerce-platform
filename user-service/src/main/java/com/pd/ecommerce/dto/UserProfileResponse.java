package com.pd.ecommerce.dto;

import com.pd.ecommerce.entity.UserRole;
import lombok.Builder;
import java.util.UUID;

@Builder
public record UserProfileResponse(
	UUID id,
	String email,
	String firstName,
	String lastName,
	UserRole role
) {}