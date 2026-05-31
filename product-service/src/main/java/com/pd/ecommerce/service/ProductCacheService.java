package com.pd.ecommerce.service;

import com.pd.ecommerce.dto.ProductPageResponse;
import com.pd.ecommerce.dto.ProductResponse;
import reactor.core.publisher.Mono;

public interface ProductCacheService {

	Mono<ProductResponse> getProduct(String key);
	Mono<ProductResponse> putProduct(String key, ProductResponse response);
	Mono<ProductPageResponse> getProducts(String key);
	Mono<Boolean> putProducts(String key, ProductPageResponse value);
	String key(String category, int pageSize, String cursor);
}
