package com.pd.ecommerce.repository;

import com.pd.ecommerce.entity.Inventory;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import java.util.UUID;

public interface ItemRepository extends ReactiveCrudRepository<Inventory, UUID> {}