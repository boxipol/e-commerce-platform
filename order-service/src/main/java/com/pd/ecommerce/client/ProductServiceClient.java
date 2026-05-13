package com.pd.ecommerce.client;

import com.pd.ecommerce.dto.ProductSnapshot;
import reactor.core.publisher.Mono;
import java.util.List;
import java.util.UUID;

public interface ProductServiceClient {

	Mono<List<ProductSnapshot>> getProducts(List<UUID> productIds);
}