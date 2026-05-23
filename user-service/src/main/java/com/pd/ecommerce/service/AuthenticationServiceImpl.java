package com.pd.ecommerce.service;

import com.pd.ecommerce.dto.AuthResponse;
import com.pd.ecommerce.dto.UserLoginRequest;
import com.pd.ecommerce.dto.UserProfileResponse;
import com.pd.ecommerce.dto.UserRegisterRequest;
import com.pd.ecommerce.dto.UserUpdateRequest;
import com.pd.ecommerce.entity.User;
import com.pd.ecommerce.entity.UserRole;
import com.pd.ecommerce.event.UserCreatedEvent;
import com.pd.ecommerce.event.UserDeletedEvent;
import com.pd.ecommerce.event.UserUpdatedEvent;
import com.pd.ecommerce.exception.EmailAlreadyExistsException;
import com.pd.ecommerce.kafka.UserEventProducer;
import com.pd.ecommerce.repository.UserRepository;
import com.pd.ecommerce.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

	private final JwtService jwtService;
	private final UserRepository repository;
	private final PasswordEncoder passwordEncoder;
	private final UserEventProducer eventProducer;


	@Override
	public Mono<UserProfileResponse> getProfile() {
		return SecurityUtils.getCurrentUserId()
			.flatMap(repository::findById)
			.switchIfEmpty(Mono.error(
				new IllegalStateException("User not found")
			))
			.map(this::toResponse);
	}

	@Override
	public Mono<AuthResponse> register(UserRegisterRequest request) {
		return repository.findByEmail(request.email())
			.flatMap(existing ->
				Mono.<User>error(new EmailAlreadyExistsException(request.email()))
			)
			.switchIfEmpty(Mono.defer(() -> create(request)))
			.map(this::buildResponse);
	}

	@Override
	public Mono<AuthResponse> login(UserLoginRequest request) {
		return repository.findByEmail(request.email())
			.switchIfEmpty(Mono.error(new IllegalStateException("User not found")))
			.filter(user ->
				passwordEncoder.matches(
					request.password(),
					user.getPassword()
				)
			)
			.switchIfEmpty(Mono.error(new RuntimeException("Invalid credentials")))
			.map(this::buildResponse);
	}

	@Override
	public Mono<Void> update(UserUpdateRequest request) {
		return repository.findByEmail(request.email())
			.switchIfEmpty(
				Mono.error(new IllegalStateException("User not found")))
			.flatMap(user -> {
				applyUpdate(user, request);
				repository.save(user)
					.doOnSuccess(saved -> {
						var event = new UserUpdatedEvent(user.getEmail(), Instant.now());
						eventProducer.sendUserUpdated(event);
					});

				return Mono.empty();
			});
	}

	@Override
	public Mono<Void> delete(UUID id) {
		return repository.findById(id)
			.switchIfEmpty(Mono.error(new RuntimeException("User not found with id: " + id)))
			.flatMap(user ->
				repository.delete(user)
					.doOnSuccess(v -> {
						var event = new UserDeletedEvent(
							user.getEmail(),
							Instant.now()
						);

						eventProducer.sendUserDeleted(event);
					})
			);
	}

//	==================== PRIVATE ====================

	private Mono<User> create(UserRegisterRequest request) {
		var user = User.builder()
			.email(request.email())
			.firstName(request.firstName())
			.lastName(request.lastName())
			.createdAt(Instant.now())
			.password(passwordEncoder.encode(request.password()))
			.role(UserRole.CUSTOMER)
			.createdAt(Instant.now())
			.build();

		return repository.save(user)
			.doOnSuccess(saved -> {
				var event = new UserCreatedEvent(user.getEmail(), Instant.now());
				eventProducer.sendUserRegistered(event);
			});
	}

	private void applyUpdate(User user, UserUpdateRequest request) {
		if (request.email() != null) {
			user.setEmail(request.email());
		}
	}

	private AuthResponse buildResponse(User user) {
		return AuthResponse.builder()
			.accessToken(jwtService.generateToken(user))
			.tokenType("Bearer")
			.expiresIn(3600L)
			.build();
	}

	private UserProfileResponse toResponse(User user) {
		return UserProfileResponse.builder()
			.id(user.getId())
			.email(user.getEmail())
			.firstName(user.getFirstName())
			.lastName(user.getLastName())
			.role(user.getRole())
			.build();
	}
}