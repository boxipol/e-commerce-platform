package com.pd.ecommerce.service;

import com.pd.ecommerce.entity.User;
import com.pd.ecommerce.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public final class CustomUserDetailsService implements UserDetailsService {

	private final UserRepository repository;


	@Override
	public UserDetails loadUserByUsername(String email) {
		User user = repository.findByEmail(email)
			.switchIfEmpty(
				Mono.error(new UsernameNotFoundException("User not found")))
			.block();

		return org.springframework.security.core.userdetails.User.withUsername(user.getEmail())
			.password(user.getPassword())
			.roles(user.getRole().toString())
			.build();
	}
}