package com.pd.ecommerce.service;

import com.pd.ecommerce.entity.Product;
import com.pd.ecommerce.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;


@Slf4j
@Service
@RequiredArgsConstructor
final class ProductServiceImpl implements ProductService {

	private final ProductRepository productRepository;


	public Mono<String> getData() {
		return Mono.just("Product Service is up and running!");
	}

	public Mono<Product> saveProduct(Product product) {
		return Mono.just(productRepository.save(product));
	}
}