package com.pd.ecommerce.service;

import com.pd.ecommerce.dto.AuthResponse;
import com.pd.ecommerce.dto.UserDeleteRequest;
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

@Slf4j
@Service
@RequiredArgsConstructor
public final class UserServiceImpl implements UserService {

	private final JwtService jwtService;
	private final UserRepository repository;
	private final PasswordEncoder passwordEncoder;
	private final UserEventProducer eventProducer;


	@Override
	public Mono<UserProfileResponse> getProfile() {
		return SecurityUtils.getCurrentUserId()
			.flatMap(repository::findById)
			.switchIfEmpty(
				Mono.error(new IllegalStateException("User not found")))
			.map(this::toResponse);
	}

	@Override
	public Mono<AuthResponse> register(UserRegisterRequest request) {
		return repository.findByEmail(request.email())
			.flatMap(existing ->
				Mono.<User>error(new EmailAlreadyExistsException(request.email()))
			)
			.switchIfEmpty(Mono.defer(() -> create(request)))
			.doOnNext(user ->
				log.info("Register successful for userId={}, email={}", user.getId(), user.getEmail())
			)
			.map(this::buildResponse)
			.doOnError(e ->
				log.warn("Register failed for email={}", request.email())
			);
	}

	@Override
	public Mono<AuthResponse> login(UserLoginRequest request) {
		return repository.findByEmail(request.email())
			.switchIfEmpty(
				Mono.error(new IllegalStateException("User not found")))
			.filter(user ->
				passwordEncoder.matches(request.password(), user.getPassword())
			)
			.switchIfEmpty(Mono.error(new RuntimeException("Invalid credentials")))
			.doOnNext(user ->
				log.info("Login successful for userId={}, email={}", user.getId(), user.getEmail())
			)
			.map(this::buildResponse)
			.doOnError(e ->
				log.warn("Login failed for email={}", request.email())
			);
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
						var event = new UserUpdatedEvent(saved.getEmail(), Instant.now());
						eventProducer.sendUserUpdated(event);

						log.info("Update successful for userId={}, email={}", saved.getId(), saved.getEmail());
					});

				return Mono.empty();
			});
	}

	@Override
	public Mono<Void> delete(UserDeleteRequest request) {
		return repository.findByEmail(request.email())
			.switchIfEmpty(
				Mono.error(new RuntimeException("User not found with email: " + request.email())))
			.flatMap(user ->
				repository.delete(user)
					.doOnSuccess(v -> {
						var event = new UserDeletedEvent(user.getEmail(), Instant.now());
						eventProducer.sendUserDeleted(event);

						log.info("Delete successful for userId={}, email={}", user.getId(), user.getEmail());
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
			.expiresIn(3_600L)
			.build();
	}

	private UserProfileResponse toResponse(User user) {
		return UserProfileResponse.builder()
			.email(user.getEmail())
			.firstName(user.getFirstName())
			.lastName(user.getLastName())
			.role(user.getRole())
			.build();
	}
}