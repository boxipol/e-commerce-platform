package com.pd.ecommerce.service;

import com.pd.ecommerce.dto.ProductResponse;


public interface CatalogService {

	ProductResponse getProduct(Long productId);
//	ProductResponse putProduct(ProductRequest request);
}