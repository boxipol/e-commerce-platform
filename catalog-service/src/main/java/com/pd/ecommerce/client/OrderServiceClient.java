package com.pd.ecommerce.client;

import com.pd.ecommerce.config.FeignConfig;
import com.pd.ecommerce.dto.OrderResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;


@FeignClient(
	name = "order-service",                // must match Eureka service ID
	path = "/api/orders",                  // base path
	configuration = FeignConfig.class      // optional custom config
)
public interface OrderServiceClient {

	@CircuitBreaker(name = "orderService", fallbackMethod = "fallbackOrder")
	@GetMapping("/{id}")
	OrderResponse getOrderById(@PathVariable("id") Long id);

	default OrderResponse fallbackOrder(Long id, Throwable ex) {
		return new OrderResponse(id, "UNKNOWN", null);
	}
}