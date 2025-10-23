package com.pd.ecommerce.service;

import com.pd.ecommerce.dto.ProductResponse;
import com.pd.ecommerce.dto.ProductStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


@Slf4j
@Service
@RequiredArgsConstructor
final class CatalogServiceImpl implements CatalogService {

	@Override
	public ProductResponse getProduct(Long productId) {
		return new ProductResponse(101L, ProductStatus.AVAILABLE);
	}
}