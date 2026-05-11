package com.pd.ecommerce.service;

import com.pd.ecommerce.entity.Product;
import com.pd.ecommerce.entity.ProductByCategory;
import com.pd.ecommerce.repository.ProductByCategoryRepository;
import com.pd.ecommerce.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
final class ProductServiceImpl implements ProductService {

	private final ProductRepository productRepository;
	private final ProductByCategoryRepository productByCategoryRepository;


	public Mono<Product> getById(UUID id) {
		return productRepository.findById(id);
	}

	public Flux<Product> getAll() {
		return productRepository.findAll();
	}

	public Mono<Product> create(Product product) {
		return productRepository.save(product);
	}

	public Mono<Void> delete(UUID id) {
		return productRepository.deleteById(id);
	}

	public Flux<ProductByCategory> getByCategory(String category) {
		return productByCategoryRepository.findByKeyCategory(category);
	}
}