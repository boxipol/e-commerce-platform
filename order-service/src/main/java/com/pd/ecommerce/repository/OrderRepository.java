package com.pd.ecommerce.repository;

import com.pd.ecommerce.entity.Order;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import java.util.UUID;

public interface OrderRepository extends ReactiveCrudRepository<Order, UUID> {}