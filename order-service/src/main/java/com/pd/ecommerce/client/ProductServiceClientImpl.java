package com.pd.ecommerce.client;

import com.pd.ecommerce.dto.ProductSnapshot;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import java.util.List;

@Service
public final class ProductServiceClientImpl implements ProductServiceClient {

	private final WebClient webClient;


	public ProductServiceClientImpl(WebClient.Builder builder, @Value("${services.product.url}") String productServiceUrl) {
		this.webClient = builder.baseUrl(productServiceUrl).build();
	}

	@Override
	public Mono<List<ProductSnapshot>> getProducts(List<String> productSkus) {
		return webClient.get()
			.uri(uriBuilder -> uriBuilder.path("/api/v1/products/batch")
				.queryParam("skus", productSkus)
				.build())
			.retrieve()
			.bodyToFlux(ProductSnapshot.class)
			.collectList();
	}
}