package com.pd.ecommerce.dto;

import com.pd.ecommerce.entity.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UserDeleteRequest(
	@NotBlank(message = "Email is required")
	@Email(message = "Invalid email format")
	String email,

	@NotNull(message = "Role is required")
	UserRole role,

	@NotBlank(message = "Password is required")
	@Size(min = 6, message = "Password must be at least 6 characters")
	String password
) {}