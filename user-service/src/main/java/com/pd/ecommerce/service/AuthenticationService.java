package com.pd.ecommerce.service;

import com.pd.ecommerce.dto.AuthResponse;
import com.pd.ecommerce.dto.UserLoginRequest;
import com.pd.ecommerce.dto.UserProfileResponse;
import com.pd.ecommerce.dto.UserRegisterRequest;
import com.pd.ecommerce.dto.UserUpdateRequest;
import reactor.core.publisher.Mono;
import java.util.UUID;

public interface AuthenticationService {

	Mono<UserProfileResponse> getProfile();
	Mono<AuthResponse> register(UserRegisterRequest request);
	Mono<AuthResponse> login(UserLoginRequest request);
	Mono<Void> update(UserUpdateRequest request);
	Mono<Void> delete(UUID id);
}