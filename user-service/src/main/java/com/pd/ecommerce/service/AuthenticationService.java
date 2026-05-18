package com.pd.ecommerce.service;

import com.pd.ecommerce.dto.AuthResponse;
import com.pd.ecommerce.dto.LoginRequest;
import com.pd.ecommerce.dto.RegisterRequest;
import reactor.core.publisher.Mono;

public interface AuthenticationService {

	Mono<AuthResponse> register(RegisterRequest request);
	Mono<AuthResponse> login(LoginRequest request);
}