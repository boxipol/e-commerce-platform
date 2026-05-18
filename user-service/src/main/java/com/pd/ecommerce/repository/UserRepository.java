package com.pd.ecommerce.repository;

import com.pd.ecommerce.entity.User;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;
import java.util.UUID;

public interface UserRepository extends ReactiveCrudRepository<User, UUID> {

	Mono<User> findByEmail(String email);
}