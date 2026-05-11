package com.pd.ecommerce.service;

import com.pd.ecommerce.dto.ProductByCategoryView;
import com.pd.ecommerce.dto.ProductCreateRequest;
import com.pd.ecommerce.dto.ProductResponse;
import com.pd.ecommerce.dto.ProductUpdateRequest;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.UUID;

@Service
public interface ProductService {

	Mono<ProductResponse> getById(UUID id);
	Flux<ProductResponse> getAll();
	Mono<ProductResponse> create(ProductCreateRequest product);
	Mono<ProductResponse> update(UUID id, ProductUpdateRequest product);
	Mono<Void> delete(UUID id);
	Flux<ProductByCategoryView> getByCategory(String category);
}