package com.pd.ecommerce.controller;

import com.pd.ecommerce.dto.AuthResponse;
import com.pd.ecommerce.dto.UserDeleteRequest;
import com.pd.ecommerce.dto.UserLoginRequest;
import com.pd.ecommerce.dto.UserProfileResponse;
import com.pd.ecommerce.dto.UserRegisterRequest;
import com.pd.ecommerce.dto.UserUpdateRequest;
import com.pd.ecommerce.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@Tag(name = "Users", description = "Authentication and user account management")
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public final class UserController {

	private final UserService service;


	@Operation(summary = "Get current user profile", description = "Returns the authenticated user's profile (identity resolved from JWT via X-User-Id header)")
	@GetMapping("/me")
	public Mono<UserProfileResponse> me() {
		return service.getProfile();
	}

	@Operation(summary = "Register new user", description = "Creates a new account and returns a signed JWT")
	@PostMapping("/register")
	public Mono<AuthResponse> register(@Valid @RequestBody UserRegisterRequest request) {
		return service.register(request);
	}

	@Operation(summary = "Login", description = "Authenticates credentials and returns a signed JWT")
	@PostMapping("/login")
	public Mono<AuthResponse> login(@Valid @RequestBody UserLoginRequest request) {
		return service.login(request);
	}

	@Operation(summary = "Update user profile", description = "Partial update of the authenticated user's details")
	@PatchMapping("/update")
	public Mono<Void> update(@Valid @RequestBody UserUpdateRequest request) {
		return service.update(request);
	}

	@Operation(summary = "Delete account", description = "Permanently deletes the authenticated user's account")
	@DeleteMapping("/delete")
	public Mono<Void> delete(@Valid @RequestBody UserDeleteRequest request) {
		return service.delete(request);
	}
}