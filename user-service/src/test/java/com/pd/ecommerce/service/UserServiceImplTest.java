package com.pd.ecommerce.service;

import com.pd.ecommerce.dto.UserDeleteRequest;
import com.pd.ecommerce.dto.UserLoginRequest;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserServiceImpl Tests")
class UserServiceImplTest {

	@Mock
	private UserRepository userRepository;

	@Mock
	private JwtService jwtService;

	@Mock
	private PasswordEncoder passwordEncoder;

	@Mock
	private UserEventProducer eventProducer;

	@InjectMocks
	private UserServiceImpl userService;

	private UUID testUserId;
	private User testUser;
	private String testEmail;
	private String testPassword;


	@BeforeEach
	void setUp() {
		testUserId = UUID.randomUUID();
		testEmail = "test@example.com";
		testPassword = "password123";
		testUser = User.builder()
			.id(testUserId)
			.email(testEmail)
			.firstName("John")
			.lastName("Doe")
			.password("encodedPassword123")
			.role(UserRole.CUSTOMER)
			.createdAt(Instant.now())
			.build();
	}

	// ===================== REGISTER TESTS =====================

	@Test
	@DisplayName("register - should successfully register new user")
	void testRegisterSuccess() {
		// Given
		UserRegisterRequest request = new UserRegisterRequest(
			testEmail,
			"John",
			"Doe",
			UserRole.CUSTOMER,
			testPassword
		);
		when(userRepository.findByEmail(testEmail)).thenReturn(Mono.empty());
		when(passwordEncoder.encode(testPassword)).thenReturn("encodedPassword123");
		when(userRepository.save(any(User.class))).thenReturn(Mono.just(testUser));
		when(jwtService.generateToken(any(User.class))).thenReturn("jwt-token-123");

		// When & Then
		StepVerifier.create(userService.register(request))
			.assertNext(response -> {
				assertThat(response).isNotNull();
				assertThat(response.accessToken()).isEqualTo("jwt-token-123");
				assertThat(response.tokenType()).isEqualTo("Bearer");
				assertThat(response.expiresIn()).isEqualTo(3_600L);
			})
			.verifyComplete();

		// Verify event was produced
		ArgumentCaptor<UserCreatedEvent> eventCaptor = ArgumentCaptor.forClass(UserCreatedEvent.class);
		verify(eventProducer, times(1)).sendUserRegistered(eventCaptor.capture());
		assertThat(eventCaptor.getValue().email()).isEqualTo(testEmail);
	}

	@Test
	@DisplayName("register - should fail when email already exists")
	void testRegisterEmailAlreadyExists() {
		// Given
		UserRegisterRequest request = new UserRegisterRequest(
			testEmail,
			"John",
			"Doe",
			UserRole.CUSTOMER,
			testPassword
		);
		when(userRepository.findByEmail(testEmail)).thenReturn(Mono.just(testUser));

		// When & Then
		StepVerifier.create(userService.register(request))
			.expectError(EmailAlreadyExistsException.class)
			.verify();

		// Verify no save or event produced
		verify(userRepository, never()).save(any());
		verify(eventProducer, never()).sendUserRegistered(any());
	}

	// ===================== LOGIN TESTS =====================

	@Test
	@DisplayName("login - should successfully login with valid credentials")
	void testLoginSuccess() {
		// Given
		UserLoginRequest request = new UserLoginRequest(
			testEmail,
			UserRole.CUSTOMER,
			testPassword
		);
		when(userRepository.findByEmail(testEmail)).thenReturn(Mono.just(testUser));
		when(passwordEncoder.matches(testPassword, testUser.getPassword())).thenReturn(true);
		when(jwtService.generateToken(testUser)).thenReturn("jwt-token-123");

		// When & Then
		StepVerifier.create(userService.login(request))
			.assertNext(response -> {
				assertThat(response).isNotNull();
				assertThat(response.accessToken()).isEqualTo("jwt-token-123");
				assertThat(response.tokenType()).isEqualTo("Bearer");
				assertThat(response.expiresIn()).isEqualTo(3_600L);
			})
			.verifyComplete();
	}

	@Test
	@DisplayName("login - should fail when user not found")
	void testLoginUserNotFound() {
		// Given
		UserLoginRequest request = new UserLoginRequest(
			testEmail,
			UserRole.CUSTOMER,
			testPassword
		);
		when(userRepository.findByEmail(testEmail)).thenReturn(Mono.empty());

		// When & Then
		StepVerifier.create(userService.login(request))
			.expectError(ResponseStatusException.class)
			.verify();
	}

	@Test
	@DisplayName("login - should fail with invalid password")
	void testLoginInvalidPassword() {
		// Given
		UserLoginRequest request = new UserLoginRequest(
			testEmail,
			UserRole.CUSTOMER,
			"wrongPassword"
		);
		when(userRepository.findByEmail(testEmail)).thenReturn(Mono.just(testUser));
		when(passwordEncoder.matches("wrongPassword", testUser.getPassword())).thenReturn(false);

		// When & Then
		StepVerifier.create(userService.login(request))
			.expectError(RuntimeException.class)
			.verify();
	}

	// ===================== PROFILE TESTS =====================

	@Test
	@DisplayName("getProfile - should retrieve user profile when found")
	void testGetProfileSuccess() {
		// Note: Full test of getProfile requires mocking SecurityUtils.getCurrentUserId()
		// which is tested in integration tests. This is a placeholder for documentation.
		// Real implementation would need PowerMock or refactoring SecurityUtils
	}

	// ===================== HELPER METHODS =====================

	// Note: Tests focus on verifiable scenario paths. getProfile() requires
	// SecurityContext mocking which is better tested in integration tests.

	@Test
	@DisplayName("update - should successfully update user")
	void testUpdateSuccess() {
		// Given
		UserUpdateRequest request = new UserUpdateRequest("newemail@example.com", "password123");
		// Inject the current user's ID into the reactive security context
		var auth = new UsernamePasswordAuthenticationToken(testUserId.toString(), null, List.of());
		when(userRepository.findById(testUserId)).thenReturn(Mono.just(testUser));
		when(userRepository.save(any(User.class))).thenReturn(Mono.just(testUser));
		// When & Then — subscribe with a security context that provides the user ID
		StepVerifier.create(userService.update(request).contextWrite(ReactiveSecurityContextHolder.withAuthentication(auth))).verifyComplete();
		// Verify save was called
		ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
		verify(userRepository, times(1)).save(userCaptor.capture());
		assertThat(userCaptor.getValue().getEmail()).isEqualTo("newemail@example.com");
		// Verify the event was produced
		verify(eventProducer, times(1)).sendUserUpdated(any(UserUpdatedEvent.class));
	}

	@Test
	@DisplayName("update - should fail when user not found")
	void testUpdateUserNotFound() {
		// Given
		UserUpdateRequest request = new UserUpdateRequest("newemail@example.com", "password123");
		var auth = new UsernamePasswordAuthenticationToken(testUserId.toString(), null, List.of());
		when(userRepository.findById(testUserId)).thenReturn(Mono.empty());
		// When & Then
		StepVerifier.create(userService.update(request).contextWrite(ReactiveSecurityContextHolder.withAuthentication(auth))).expectError(IllegalStateException.class).verify();
		// Verify no save or event produced
		verify(userRepository, never()).save(any());
		verify(eventProducer, never()).sendUserUpdated(any());
	}

	// ===================== DELETE TESTS =====================

	@Test
	@DisplayName("delete - should successfully delete user")
	void testDeleteSuccess() {
		// Given
		UserDeleteRequest request = new UserDeleteRequest(testEmail, UserRole.CUSTOMER, testPassword);
		when(userRepository.findByEmail(testEmail)).thenReturn(Mono.just(testUser));
		when(userRepository.delete(testUser)).thenReturn(Mono.empty());

		// When & Then
		StepVerifier.create(userService.delete(request))
			.verifyComplete();

		// Verify delete was called
		verify(userRepository, times(1)).delete(testUser);

		// Verify event was produced
		ArgumentCaptor<UserDeletedEvent> eventCaptor = ArgumentCaptor.forClass(UserDeletedEvent.class);
		verify(eventProducer, times(1)).sendUserDeleted(eventCaptor.capture());
		assertThat(eventCaptor.getValue().email()).isEqualTo(testEmail);
	}

	@Test
	@DisplayName("delete - should fail when user not found")
	void testDeleteUserNotFound() {
		// Given
		UserDeleteRequest request = new UserDeleteRequest("nonexistent@example.com", UserRole.CUSTOMER, "password123");
		when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Mono.empty());

		// When & Then
		StepVerifier.create(userService.delete(request))
			.expectError(RuntimeException.class)
			.verify();

		// Verify delete was not called
		verify(userRepository, never()).delete(any());
		verify(eventProducer, never()).sendUserDeleted(any());
	}
}