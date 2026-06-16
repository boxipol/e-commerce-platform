# User Service Unit Tests

This directory contains comprehensive unit tests for the User Service component.

## Test Coverage

### Service Layer Tests

#### `UserServiceImplTest.java`
Tests for the main business logic of the UserService:
- **Registration**: Valid registration, duplicate email handling
- **Login**: Valid credentials, invalid credentials, user not found
- **Update**: User updates, error handling
- **Delete**: User deletion, error handling
- **Event Publishing**: Verification that Kafka events are published

Uses:
- Mockito for dependency mocking
- Reactor StepVerifier for reactive stream testing
- ArgumentCaptor for event verification

### JWT Token Tests

#### `JwtServiceTest.java`
Tests for JWT token generation and validation:
- **Token Generation**: Valid token structure, correct format
- **Token Claims**: Includes user ID, email, and role
- **Expiration**: Proper expiration time calculation
- **Token Parsing**: Valid signature verification
- **Role Support**: Different roles are correctly encoded
- **Multiple Users**: Different tokens for different users

Uses:
- JJWT library for token generation and parsing
- Base64 encoding/decoding for secret keys

### Controller Tests

#### `UserControllerTest.java`
Tests for REST endpoint validation:
- **Register Endpoint**: Valid registration, validation errors
- **Login Endpoint**: Valid login, invalid credentials
- **Update Endpoint**: User updates, error handling
- **Delete Endpoint**: User deletion, error handling

Uses:
- WebTestClient for reactive REST testing
- MockMvc concepts applied to WebFlux

### Event Producer Tests

#### `UserEventProducerTest.java`
Tests for Kafka event publishing:
- **User Created Events**: Verification of user.created topic
- **User Updated Events**: Verification of user.updated topic
- **User Deleted Events**: Verification of user.deleted topic
- **Event Properties**: Email keys, timestamps, payloads

Uses:
- Mockito for KafkaTemplate mocking
- ArgumentCaptor for event captures

## Test Data Builders

### `TestDataBuilder.java`
Fluent builder pattern for creating test data:
- `aUser()` - Create User entities
- `aUserRegisterRequest()` - Create registration requests
- `aUserLoginRequest()` - Create login requests
- `aUserUpdateRequest()` - Create update requests
- `aUserDeleteRequest()` - Create delete requests
- `aUserProfileResponse()` - Create profile responses

Example usage:
```java
User testUser = aUser()
    .withEmail("test@example.com")
    .withRole(UserRole.ADMIN)
    .build();

UserRegisterRequest request = aUserRegisterRequest()
    .withEmail("newuser@example.com")
    .withPassword("securePassword123")
    .build();
```

## Running the Tests

### Using Maven
```bash
# Run all user service tests
mvn -f user-service/pom.xml test

# Run a specific test class
mvn -f user-service/pom.xml test -Dtest=UserServiceImplTest

# Run a specific test method
mvn -f user-service/pom.xml test -Dtest=UserServiceImplTest#testRegisterSuccess
```

### Using IDE
- Right-click on test class or method and select "Run"
- Or use the test runner in your IDE (IntelliJ IDEA, VSCode, etc.)

##Test Configuration

### Dependencies
The tests use the following dependencies (defined in `pom.xml`):
- `spring-boot-starter-test` - Core testing framework
- `reactor-test` - Reactive stream testing (StepVerifier)
- `assertj-core` - Fluent assertions
- `spring-kafka-test` - Kafka testing utilities
- `mockito-core` - Mocking framework
- `spring-security-test` - Security testing support

### Properties
Tests use default Spring Boot test configurations. No special configuration required.

## Test Patterns Used

### Given-When-Then
All tests follow the Given-When-Then structure:
```java
@Test
void testExample() {
    // Given - Setup test data and mocks
    UserRegisterRequest request = new UserRegisterRequest(...);
    when(userRepository.findByEmail(...)).thenReturn(Mono.empty());

    // When - Execute the test
    Mono<AuthResponse> result = userService.register(request);

    // Then - Verify the outcome
    StepVerifier.create(result)
        .assertNext(response -> {
            assertThat(response).isNotNull();
        })
        .verifyComplete();
}
```

### Reactive Testing with StepVerifier
Tests verify reactive chain execution:
```java
StepVerifier.create(userService.login(request))
    .assertNext(response -> {
        assertThat(response.accessToken()).isNotEmpty();
    })
    .verifyComplete();
```

### ArgumentCaptor for Event Verification
Tests verify events are published correctly:
```java
ArgumentCaptor<UserCreatedEvent> captor = ArgumentCaptor.forClass(UserCreatedEvent.class);
verify(eventProducer).sendUserRegistered(captor.capture());
assertThat(captor.getValue().email()).isEqualTo("test@example.com");
```

## Coverage Goals

- **Service Layer**: ~90% coverage for UserServiceImpl methods
- **JWT Service**: ~95% coverage for token generation and parsing
- **Controllers**: ~85% coverage for endpoint validation
- **Event Publishing**: ~100% coverage for Kafka integration

##Known Limitations

1. **getProfile() Full Test**: SecurityContext mocking is complex and requires PowerMock or SecurityContextHolder manipulation. This is better tested in integration tests.

2. **Database Integration**: Tests use mocked repositories. Full integration tests would use an embedded database or test containers.

3. **Kafka Integration**: Tests use mocked KafkaTemplate. Full integration tests would use EmbeddedKafka or test containers.

## Future Enhancements

- [ ] Add integration tests with EmbeddedKafka
- [ ] Add performance tests with reactive streams
- [ ] Add parameterized tests for multiple user roles
- [ ] Add test coverage for exception handlers
- [ ] Add contract tests for API endpoints

## References

- [JUnit 5 Documentation](https://junit.org/junit5/docs/)
- [Mockito Documentation](https://javadoc.io/doc/org.mockito/mockito-core)
- [Reactor Test Documentation](https://projectreactor.io/docs/core/release/reference/testing.html)
- [Spring Boot Testing Guide](https://spring.io/guides/gs/testing-web/)

