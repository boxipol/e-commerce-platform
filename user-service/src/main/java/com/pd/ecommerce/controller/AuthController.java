package com.pd.ecommerce.controller;

import com.pd.ecommerce.dto.AuthResponse;
import com.pd.ecommerce.dto.UserLoginRequest;
import com.pd.ecommerce.dto.UserProfileResponse;
import com.pd.ecommerce.dto.UserRegisterRequest;
import com.pd.ecommerce.dto.UserUpdateRequest;
import com.pd.ecommerce.service.AuthenticationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public final class AuthController {

	private final AuthenticationService service;


	@GetMapping("/me")
	public Mono<UserProfileResponse> me() {
		return service.getProfile();
	}

	@PostMapping("/register")
	public Mono<AuthResponse> register(@Valid @RequestBody UserRegisterRequest request) {
		return service.register(request);
	}

	@PostMapping("/login")
	public Mono<AuthResponse> login(@Valid @RequestBody UserLoginRequest request) {
		return service.login(request);
	}

	@PatchMapping("/update")
	public Mono<Void> update(@Valid @RequestBody UserUpdateRequest request) {
		return service.update(request);
	}

	@DeleteMapping("/delete")
	public Mono<Void> delete(@RequestParam UUID id) {
		return service.delete(id);
	}
}