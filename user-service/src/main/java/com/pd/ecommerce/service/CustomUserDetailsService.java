package com.pd.ecommerce.service;

import com.pd.ecommerce.models.User;
import com.pd.ecommerce.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

	private final UserRepository repository;


	@Override
	public UserDetails loadUserByUsername(String email) {
		User user = repository.findByEmail(email).orElseThrow();
		return org.springframework.security.core.userdetails.User.withUsername(user.getEmail())
			.password(user.getPassword())
			.roles(user.getRole())
			.build();
	}
}