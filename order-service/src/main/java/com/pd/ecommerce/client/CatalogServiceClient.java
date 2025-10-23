package com.pd.ecommerce.client;

import com.pd.ecommerce.config.FeignConfig;
import com.pd.ecommerce.dto.OrderResponse;
import com.pd.ecommerce.dto.ProductResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;


@FeignClient(
	name = "catalog-service",
	path = "/api/v1/products",
	configuration = FeignConfig.class
)
public interface CatalogServiceClient {

	@CircuitBreaker(name = "catalogService", fallbackMethod = "fallbackOrder")
	@GetMapping("/{id}")
	ProductResponse getProduct(@PathVariable("id") Long id);

	default OrderResponse fallbackOrder(Long id, Throwable ex) {
		return new OrderResponse(id, "UNKNOWN", null);
	}
}