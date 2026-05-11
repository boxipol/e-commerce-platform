package com.pd.ecommerce.repository;

import com.pd.ecommerce.entity.Product;
import org.springframework.data.cassandra.repository.ReactiveCassandraRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.UUID;

@Repository
public interface ProductRepository extends ReactiveCassandraRepository<Product, UUID> {

	Mono<Product> findById(UUID id);
	Flux<Product> findAll();
}