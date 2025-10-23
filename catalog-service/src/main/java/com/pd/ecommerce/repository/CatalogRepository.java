package com.pd.ecommerce.repository;

import com.pd.ecommerce.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;


public interface CatalogRepository extends JpaRepository<Product, Long> {

	List<Product> findByProductId(Long userId);
}