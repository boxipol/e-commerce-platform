package com.pd.ecommerce.service;

import com.pd.ecommerce.dto.ProductCreateRequest;
import com.pd.ecommerce.dto.ProductPageResponse;
import com.pd.ecommerce.dto.ProductResponse;
import com.pd.ecommerce.dto.ProductUpdateRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.List;
import java.util.UUID;

public interface ProductService {

	Mono<ProductResponse> getById(UUID id);
	Flux<ProductResponse> getProducts(List<UUID> ids);
	Mono<ProductPageResponse> getByCategory(String category, int pageSize, String pageNumber);
	Mono<ProductResponse> create(ProductCreateRequest product);
	Mono<ProductResponse> update(UUID id, ProductUpdateRequest product);
	Mono<Void> delete(UUID id);
}