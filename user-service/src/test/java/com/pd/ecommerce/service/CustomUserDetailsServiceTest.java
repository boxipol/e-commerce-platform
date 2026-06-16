package com.pd.ecommerce.service;

import com.pd.ecommerce.entity.User;
import com.pd.ecommerce.entity.UserRole;
import com.pd.ecommerce.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CustomUserDetailsService Tests")
class CustomUserDetailsServiceTest {

	@Mock
	private UserRepository repository;

	@InjectMocks
	private CustomUserDetailsService service;


	@Test
	@DisplayName("loadUserByUsername - should map user to Spring UserDetails")
	void testLoadUserSuccess() {
		User user = User.builder()
			.id(UUID.randomUUID())
			.email("test@example.com")
			.password("encodedPassword")
			.role(UserRole.CUSTOMER)
			.createdAt(Instant.now())
			.build();
		when(repository.findByEmail("test@example.com")).thenReturn(Mono.just(user));

		UserDetails details = service.loadUserByUsername("test@example.com");

		assertThat(details.getUsername()).isEqualTo("test@example.com");
		assertThat(details.getPassword()).isEqualTo("encodedPassword");
		assertThat(details.getAuthorities())
			.extracting("authority")
			.contains("ROLE_CUSTOMER");
	}

	@Test
	@DisplayName("loadUserByUsername - should throw when user not found")
	void testLoadUserNotFound() {
		when(repository.findByEmail("missing@example.com")).thenReturn(Mono.empty());

		assertThatThrownBy(() -> service.loadUserByUsername("missing@example.com"))
			.isInstanceOf(UsernameNotFoundException.class);
	}
}