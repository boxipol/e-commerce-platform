package com.pd.ecommerce.service;

import com.pd.ecommerce.dto.AuthResponse;
import com.pd.ecommerce.dto.LoginRequest;
import com.pd.ecommerce.dto.RegisterRequest;
import com.pd.ecommerce.entity.User;
import com.pd.ecommerce.entity.UserRole;
import com.pd.ecommerce.event.UserCreatedEvent;
import com.pd.ecommerce.kafka.UserEventProducer;
import com.pd.ecommerce.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
final class AuthenticationServiceImpl implements AuthenticationService {

	private final JwtService jwtService;
	private final UserRepository repository;
	private final PasswordEncoder passwordEncoder;
	private final UserEventProducer eventProducer;


	@Override
	public Mono<AuthResponse> register(RegisterRequest request) {
		return repository.findByEmail(request.email())
			.flatMap(existing -> Mono.<AuthResponse>error(
				new IllegalStateException("Email already exists")))
			.switchIfEmpty(create(request));
	}

//	==================== PRIVATE ====================

	private Mono<AuthResponse> create(RegisterRequest request) {
		var user = User.builder()
			.email(request.email())
			.password(passwordEncoder.encode(request.password()))
			.role(UserRole.CUSTOMER)
			.build();

		var event = new UserCreatedEvent(user.getEmail(), Instant.now());

		return repository.save(user)
			.map(savedUser -> new AuthResponse(
				jwtService.generateToken(savedUser.getEmail(), savedUser.getRole())
			))
			.doOnSuccess(saved -> eventProducer.sendUserRegistered(event));
	}

	@Override
	public Mono<AuthResponse> login(LoginRequest request) {
		return repository.findByEmail(request.email())
			.switchIfEmpty(Mono.error(new IllegalStateException("User not found")))
			.flatMap(user -> {
				if (!passwordEncoder.matches(request.password(), user.getPassword())) {
					return Mono.error(new IllegalStateException("Invalid credentials"));
				}

				var token = jwtService.generateToken(user.getEmail(), user.getRole());

				return Mono.just(new AuthResponse(token));
			});
	}

	@Override
	public Mono<Void> delete(UUID id) {
		return repository.findById(id)
			.switchIfEmpty(
				Mono.error(new RuntimeException("User not found with id: " + id)))
			.flatMap(repository::delete);
	}
}