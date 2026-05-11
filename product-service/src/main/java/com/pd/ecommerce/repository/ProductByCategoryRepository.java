package com.pd.ecommerce.repository;

import com.pd.ecommerce.entity.ProductByCategory;
import com.pd.ecommerce.entity.ProductByCategoryKey;
import org.springframework.data.cassandra.repository.ReactiveCassandraRepository;
import reactor.core.publisher.Flux;

public interface ProductByCategoryRepository extends ReactiveCassandraRepository<ProductByCategory, ProductByCategoryKey> {

	// ✅ Efficient: single partition scan (category)
	Flux<ProductByCategory> findByKeyCategory(String category);
}