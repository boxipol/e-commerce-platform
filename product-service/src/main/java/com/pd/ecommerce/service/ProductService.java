package com.pd.ecommerce.service;

import com.pd.ecommerce.entity.Product;
import com.pd.ecommerce.entity.ProductByCategory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.UUID;

@Service
public interface ProductService {

	Mono<Product> getById(UUID id);
	Flux<Product> getAll();
	Mono<Product> create(Product product);
	Mono<Void> delete(UUID id);
	Flux<ProductByCategory> getByCategory(String category);
}