package com.pd.ecommerce.repository;

import com.pd.ecommerce.entity.Item;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import java.util.UUID;

public interface ItemRepository extends ReactiveCrudRepository<Item, UUID> {}