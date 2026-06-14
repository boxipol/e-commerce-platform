package com.pd.ecommerce.repository;

import com.pd.ecommerce.entity.ProductBySku;
import org.springframework.data.cassandra.repository.ReactiveCassandraRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductBySkuRepository extends ReactiveCassandraRepository<ProductBySku, String> {}