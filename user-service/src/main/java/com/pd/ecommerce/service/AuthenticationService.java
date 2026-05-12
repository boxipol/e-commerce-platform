package com.pd.ecommerce.service;

import com.pd.ecommerce.dto.AuthResponse;
import com.pd.ecommerce.dto.LoginRequest;
import com.pd.ecommerce.dto.RegisterRequest;

public interface AuthenticationService {

	AuthResponse register(RegisterRequest request);
	AuthResponse login(LoginRequest request);
}