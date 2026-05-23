package com.pd.ecommerce.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import java.time.Instant;
import java.util.UUID;

@Table(name = "users")
@Builder
@Getter
@Setter
public final class User {

	@Id
	private UUID id;
	private String email;
	private String firstName;
	private String lastName;
	private String password;
	private UserRole role;
	private Instant createdAt;
}