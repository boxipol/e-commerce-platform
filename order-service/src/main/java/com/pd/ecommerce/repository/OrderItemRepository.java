package com.pd.ecommerce.repository;

import com.pd.ecommerce.entity.OrderItem;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import java.util.UUID;

public interface OrderItemRepository extends ReactiveCrudRepository<OrderItem, UUID> {

	Flux<OrderItem> findByOrderId(UUID orderId);
}