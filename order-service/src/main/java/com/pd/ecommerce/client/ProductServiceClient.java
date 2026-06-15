package com.pd.ecommerce.client;

import com.pd.ecommerce.dto.ProductSnapshot;
import reactor.core.publisher.Mono;
import java.util.List;

public interface ProductServiceClient {

	Mono<List<ProductSnapshot>> getProducts(List<String> productIds);
}