package com.pd.ecommerce.service;

import com.pd.ecommerce.entity.Product;
import reactor.core.publisher.Mono;


public interface ProductService {

	Mono<String> getData();
	Mono<Product> saveProduct(Product product);
}