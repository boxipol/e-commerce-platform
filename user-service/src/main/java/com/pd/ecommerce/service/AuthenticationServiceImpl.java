package com.pd.ecommerce.service;

import com.pd.ecommerce.dto.AuthResponse;
import com.pd.ecommerce.dto.LoginRequest;
import com.pd.ecommerce.dto.RegisterRequest;
import com.pd.ecommerce.entity.User;
import com.pd.ecommerce.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
final class AuthenticationServiceImpl implements AuthenticationService {

	private final JwtService jwtService;
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;


	public AuthResponse register(RegisterRequest request) {
		if (userRepository.findByEmail(request.email()).isPresent()) {
			throw new RuntimeException("Email already exists");
		}

		User user = new User();
		user.setEmail(request.email());
		user.setPassword(passwordEncoder.encode(request.password()));
		user.setRole("USER");
		userRepository.save(user);
		String token = jwtService.generateToken(user.getEmail(), user.getRole());

		return new AuthResponse(token);
	}

	public AuthResponse login(LoginRequest request) {
		User user = userRepository.findByEmail(request.email())
			.orElseThrow();

		if (!passwordEncoder.matches(request.password(), user.getPassword())) {
			throw new RuntimeException("Invalid credentials");
		}

		String token = jwtService.generateToken(user.getEmail(), user.getRole());

		return new AuthResponse(token);
	}
}
