package com.pd.ecommerce.service;

import com.pd.ecommerce.dto.AuthResponse;
import com.pd.ecommerce.dto.LoginRequest;
import com.pd.ecommerce.dto.RegisterRequest;
import com.pd.ecommerce.entity.User;
import com.pd.ecommerce.entity.UserRole;
import com.pd.ecommerce.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
final class AuthenticationServiceImpl implements AuthenticationService {

	private final JwtService jwtService;
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;


	@Override
	public Mono<AuthResponse> register(RegisterRequest request) {
		return userRepository.findByEmail(request.email())
			.flatMap(existing -> Mono.<AuthResponse>error(
				new IllegalStateException("Email already exists")
			))
			.switchIfEmpty(createUser(request));
	}

	private Mono<AuthResponse> createUser(RegisterRequest request) {
		var user = User.builder()
			.email(request.email())
			.password(passwordEncoder.encode(request.password()))
			.role(UserRole.CUSTOMER)
			.build();

		return userRepository.save(user)
			.map(savedUser -> new AuthResponse(
				jwtService.generateToken(savedUser.getEmail(), savedUser.getRole())
			));
	}

	@Override
	public Mono<AuthResponse> login(LoginRequest request) {
		return userRepository.findByEmail(request.email())
			.switchIfEmpty(Mono.error(new IllegalStateException("User not found")))
			.flatMap(user -> {
				if (!passwordEncoder.matches(request.password(), user.getPassword())) {
					return Mono.error(new IllegalStateException("Invalid credentials"));
				}

				var token = jwtService.generateToken(user.getEmail(), user.getRole());

				return Mono.just(new AuthResponse(token));
			});
	}
}