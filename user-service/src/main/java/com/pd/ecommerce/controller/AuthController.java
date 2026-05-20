package com.pd.ecommerce.controller;

import com.pd.ecommerce.dto.AuthResponse;
import com.pd.ecommerce.dto.LoginRequest;
import com.pd.ecommerce.dto.RegisterRequest;
import com.pd.ecommerce.service.AuthenticationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
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


	@PostMapping("/register")
	public Mono<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
		return service.register(request);
	}

	@PostMapping("/login")
	public Mono<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
		return service.login(request);
	}

	// todo update

	@DeleteMapping("/delete")
	public Mono<Void> delete(@RequestParam UUID id) {
		return service.delete(id);
	}
}