package com.pd.ecommerce.controller;

import com.pd.ecommerce.dto.AuthResponse;
import com.pd.ecommerce.dto.UserDeleteRequest;
import com.pd.ecommerce.dto.UserLoginRequest;
import com.pd.ecommerce.dto.UserProfileResponse;
import com.pd.ecommerce.dto.UserRegisterRequest;
import com.pd.ecommerce.dto.UserUpdateRequest;
import com.pd.ecommerce.entity.UserRole;
import com.pd.ecommerce.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserController Tests")
class UserControllerTest {

	@Mock
	private UserService userService;

	@InjectMocks
	private UserController userController;

	private WebTestClient webTestClient;


	@BeforeEach
	void setUp() {
		webTestClient = WebTestClient.bindToController(userController)
			.build();
	}

	// ===================== REGISTER TESTS =====================

	@Test
	@DisplayName("POST /api/v1/users/register - should register user successfully")
	void testRegisterSuccess() {
		// Given
		UserRegisterRequest request = new UserRegisterRequest(
			"test@example.com",
			"John",
			"Doe",
			UserRole.CUSTOMER,
			"password123"
		);

		AuthResponse authResponse = AuthResponse.builder()
			.accessToken("jwt-token-123")
			.tokenType("Bearer")
			.expiresIn(3_600L)
			.build();

		when(userService.register(any(UserRegisterRequest.class)))
			.thenReturn(Mono.just(authResponse));

		// When & Then
		webTestClient.post()
			.uri("/api/v1/users/register")
			.contentType(MediaType.APPLICATION_JSON)
			.body(BodyInserters.fromValue(request))
			.exchange()
			.expectStatus().isOk()
			.expectBody(AuthResponse.class)
			.consumeWith(response -> {
				assert response.getResponseBody() != null;
				assert response.getResponseBody().accessToken().equals("jwt-token-123");
				assert response.getResponseBody().tokenType().equals("Bearer");
			});

		verify(userService).register(any(UserRegisterRequest.class));
	}

	@Test
	@DisplayName("POST /api/v1/users/register - should reject invalid email")
	void testRegisterInvalidEmail() {
		// Given
		UserRegisterRequest request = new UserRegisterRequest(
			"invalid-email",
			"John",
			"Doe",
			UserRole.CUSTOMER,
			"password123"
		);

		// When & Then
		webTestClient.post()
			.uri("/api/v1/users/register")
			.contentType(MediaType.APPLICATION_JSON)
			.body(BodyInserters.fromValue(request))
			.exchange()
			.expectStatus().isBadRequest();
	}

	@Test
	@DisplayName("POST /api/v1/users/register - should reject short password")
	void testRegisterPasswordTooShort() {
		// Given
		UserRegisterRequest request = new UserRegisterRequest(
			"test@example.com",
			"John",
			"Doe",
			UserRole.CUSTOMER,
			"short"
		);

		// When & Then
		webTestClient.post()
			.uri("/api/v1/users/register")
			.contentType(MediaType.APPLICATION_JSON)
			.body(BodyInserters.fromValue(request))
			.exchange()
			.expectStatus().isBadRequest();
	}

	@Test
	@DisplayName("POST /api/v1/users/register - should reject empty email")
	void testRegisterEmptyEmail() {
		// Given
		UserRegisterRequest request = new UserRegisterRequest(
			"",
			"John",
			"Doe",
			UserRole.CUSTOMER,
			"password123"
		);

		// When & Then
		webTestClient.post()
			.uri("/api/v1/users/register")
			.contentType(MediaType.APPLICATION_JSON)
			.body(BodyInserters.fromValue(request))
			.exchange()
			.expectStatus().isBadRequest();
	}

	// ===================== LOGIN TESTS =====================

	@Test
	@DisplayName("POST /api/v1/users/login - should login user successfully")
	void testLoginSuccess() {
		// Given
		UserLoginRequest request = new UserLoginRequest(
			"test@example.com",
			UserRole.CUSTOMER,
			"password123"
		);

		AuthResponse authResponse = AuthResponse.builder()
			.accessToken("jwt-token-123")
			.tokenType("Bearer")
			.expiresIn(3_600L)
			.build();

		when(userService.login(any(UserLoginRequest.class)))
			.thenReturn(Mono.just(authResponse));

		// When & Then
		webTestClient.post()
			.uri("/api/v1/users/login")
			.contentType(MediaType.APPLICATION_JSON)
			.body(BodyInserters.fromValue(request))
			.exchange()
			.expectStatus().isOk()
			.expectBody(AuthResponse.class)
			.consumeWith(response -> {
				assert response.getResponseBody() != null;
				assert response.getResponseBody().accessToken().equals("jwt-token-123");
			});

		verify(userService).login(any(UserLoginRequest.class));
	}

	@Test
	@DisplayName("POST /api/v1/users/login - should reject invalid credentials")
	void testLoginInvalidCredentials() {
		// Given
		UserLoginRequest request = new UserLoginRequest(
			"test@example.com",
			UserRole.CUSTOMER,
			"wrongpassword"
		);

		when(userService.login(any(UserLoginRequest.class)))
			.thenReturn(Mono.error(new RuntimeException("Invalid credentials")));

		// When & Then
		webTestClient.post()
			.uri("/api/v1/users/login")
			.contentType(MediaType.APPLICATION_JSON)
			.body(BodyInserters.fromValue(request))
			.exchange()
			.expectStatus().is5xxServerError();
	}

	@Test
	@DisplayName("POST /api/v1/users/login - should reject invalid email format")
	void testLoginInvalidEmailFormat() {
		// Given
		UserLoginRequest request = new UserLoginRequest(
			"not-an-email",
			UserRole.CUSTOMER,
			"password123"
		);

		// When & Then
		webTestClient.post()
			.uri("/api/v1/users/login")
			.contentType(MediaType.APPLICATION_JSON)
			.body(BodyInserters.fromValue(request))
			.exchange()
			.expectStatus().isBadRequest();
	}

	// ===================== PROFILE TESTS =====================

	@Test
	@DisplayName("GET /api/v1/users/me - should retrieve user profile")
	void testGetProfileSuccess() {
		// Given
		UserProfileResponse profileResponse = UserProfileResponse.builder()
			.email("test@example.com")
			.firstName("John")
			.lastName("Doe")
			.role(UserRole.CUSTOMER)
			.build();

		when(userService.getProfile())
			.thenReturn(Mono.just(profileResponse));

		// When & Then
		webTestClient.get()
			.uri("/api/v1/users/me")
			.exchange()
			.expectStatus().isOk()
			.expectBody(UserProfileResponse.class)
			.consumeWith(response -> {
				assert response.getResponseBody() != null;
				assert response.getResponseBody().email().equals("test@example.com");
				assert response.getResponseBody().firstName().equals("John");
			});

		verify(userService).getProfile();
	}

	@Test
	@DisplayName("GET /api/v1/users/me - should return error when profile not found")
	void testGetProfileNotFound() {
		// Given
		when(userService.getProfile())
			.thenReturn(Mono.error(new IllegalStateException("User not found")));

		// When & Then
		webTestClient.get()
			.uri("/api/v1/users/me")
			.exchange()
			.expectStatus().is5xxServerError();
	}

	// ===================== UPDATE TESTS =====================

	@Test
	@DisplayName("PATCH /api/v1/users/update - should update user successfully")
	void testUpdateSuccess() {
		// Given
		UserUpdateRequest request = new UserUpdateRequest("newemail@example.com", "password123");

		when(userService.update(any(UserUpdateRequest.class)))
			.thenReturn(Mono.empty());

		// When & Then
		webTestClient.patch()
			.uri("/api/v1/users/update")
			.contentType(MediaType.APPLICATION_JSON)
			.body(BodyInserters.fromValue(request))
			.exchange()
			.expectStatus().isOk();

		verify(userService).update(any(UserUpdateRequest.class));
	}

	@Test
	@DisplayName("PATCH /api/v1/users/update - should handle update error")
	void testUpdateError() {
		// Given
		UserUpdateRequest request = new UserUpdateRequest("newemail@example.com", "password123");

		when(userService.update(any(UserUpdateRequest.class)))
			.thenReturn(Mono.error(new IllegalStateException("User not found")));

		// When & Then
		webTestClient.patch()
			.uri("/api/v1/users/update")
			.contentType(MediaType.APPLICATION_JSON)
			.body(BodyInserters.fromValue(request))
			.exchange()
			.expectStatus().is5xxServerError();
	}

	// ===================== DELETE TESTS =====================

	@Test
	@DisplayName("DELETE /api/v1/users/delete - should delete user successfully")
	void testDeleteSuccess() {
		// Given
		UserDeleteRequest request = new UserDeleteRequest("test@example.com", UserRole.CUSTOMER, "password123");

		when(userService.delete(any(UserDeleteRequest.class)))
			.thenReturn(Mono.empty());

		// When & Then
		webTestClient.method(HttpMethod.DELETE)
			.uri("/api/v1/users/delete")
			.contentType(MediaType.APPLICATION_JSON)
			.body(BodyInserters.fromValue(request))
			.exchange()
			.expectStatus().isOk();

		verify(userService).delete(any(UserDeleteRequest.class));
	}

	@Test
	@DisplayName("DELETE /api/v1/users/delete - should handle deletion error")
	void testDeleteError() {
		// Given
		UserDeleteRequest request = new UserDeleteRequest("nonexistent@example.com", UserRole.CUSTOMER, "password123");

		when(userService.delete(any(UserDeleteRequest.class)))
			.thenReturn(Mono.error(new RuntimeException("User not found")));

		// When & Then
		webTestClient.method(HttpMethod.DELETE)
			.uri("/api/v1/users/delete")
			.contentType(MediaType.APPLICATION_JSON)
			.body(BodyInserters.fromValue(request))
			.exchange()
			.expectStatus().is5xxServerError();
	}
}