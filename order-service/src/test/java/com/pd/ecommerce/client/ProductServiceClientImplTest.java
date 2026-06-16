package com.pd.ecommerce.client;

import com.pd.ecommerce.dto.ProductSnapshot;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ProductServiceClientImpl Tests")
class ProductServiceClientImplTest {

	@Test
	@DisplayName("getProducts - should call the batch endpoint and collect the response list")
	void testGetProductsSuccess() {
		UUID productId = UUID.randomUUID();
		String json = """
			[{"productId":"%s","sku":"SKU-1","price":19.99}]
			""".formatted(productId);

		AtomicReference<URI> calledUri = new AtomicReference<>();
		ExchangeFunction exchange = request -> {
			calledUri.set(request.url());

			return Mono.just(ClientResponse.create(HttpStatus.OK)
				.header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
				.body(json)
				.build());
		};

		WebClient.Builder builder = WebClient.builder().exchangeFunction(exchange);
		ProductServiceClientImpl client = new ProductServiceClientImpl(builder, "http://product-service:8084");

		StepVerifier.create(client.getProducts(List.of("SKU-1")))
			.assertNext(products -> {
				assertThat(products).hasSize(1);
				ProductSnapshot snapshot = products.get(0);
				assertThat(snapshot.sku()).isEqualTo("SKU-1");
				assertThat(snapshot.price()).isEqualByComparingTo(new BigDecimal("19.99"));
			})
			.verifyComplete();

		assertThat(calledUri.get().toString())
			.contains("/api/v1/products/batch")
			.contains("skus=SKU-1");
	}

	@Test
	@DisplayName("getProducts - should return an empty list when no products match")
	void testGetProductsEmpty() {
		ExchangeFunction exchange = request -> Mono.just(ClientResponse.create(HttpStatus.OK)
			.header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
			.body("[]")
			.build());

		WebClient.Builder builder = WebClient.builder().exchangeFunction(exchange);
		ProductServiceClientImpl client = new ProductServiceClientImpl(builder, "http://product-service:8084");

		StepVerifier.create(client.getProducts(List.of("UNKNOWN")))
			.assertNext(products -> assertThat(products).isEmpty())
			.verifyComplete();
	}
}