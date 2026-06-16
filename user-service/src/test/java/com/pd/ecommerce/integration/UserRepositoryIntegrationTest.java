package com.pd.ecommerce.integration;

import com.pd.ecommerce.entity.User;
import com.pd.ecommerce.entity.UserRole;
import com.pd.ecommerce.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.dao.DataIntegrityViolationException;
import reactor.test.StepVerifier;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for {@link UserRepository} against a real PostgreSQL instance (Testcontainers).
 * Verifies persistence, the {@code findByEmail} derived query, and the unique email constraint.
 */
@DataR2dbcTest
@DisplayName("UserRepository Integration Tests")
class UserRepositoryIntegrationTest extends AbstractPostgresIntegrationTest {

	@Autowired
	private UserRepository repository;

	@BeforeEach
	void setUp() {
		repository.deleteAll().block();
	}

	private User newUser(String email) {
		return User.builder()
			.email(email)
			.firstName("John")
			.lastName("Doe")
			.password("encoded-password")
			.role(UserRole.CUSTOMER)
			.createdAt(Instant.now())
			.build();
	}

	@Test
	@DisplayName("save - should persist a user and assign a generated id")
	void testSaveAssignsId() {
		StepVerifier.create(repository.save(newUser("john@example.com")))
			.assertNext(saved -> {
				assertThat(saved.getId()).isNotNull();
				assertThat(saved.getEmail()).isEqualTo("john@example.com");
			})
			.verifyComplete();
	}

	@Test
	@DisplayName("findByEmail - should return the matching user")
	void testFindByEmailFound() {
		repository.save(newUser("jane@example.com")).block();

		StepVerifier.create(repository.findByEmail("jane@example.com"))
			.assertNext(user -> assertThat(user.getRole()).isEqualTo(UserRole.CUSTOMER))
			.verifyComplete();
	}

	@Test
	@DisplayName("findByEmail - should be empty when no user matches")
	void testFindByEmailMissing() {
		StepVerifier.create(repository.findByEmail("missing@example.com"))
			.verifyComplete();
	}

	@Test
	@DisplayName("save - should reject a duplicate email via the unique constraint")
	void testUniqueEmailConstraint() {
		repository.save(newUser("dup@example.com")).block();

		StepVerifier.create(repository.save(newUser("dup@example.com")))
			.expectError(DataIntegrityViolationException.class)
			.verify();
	}
}
