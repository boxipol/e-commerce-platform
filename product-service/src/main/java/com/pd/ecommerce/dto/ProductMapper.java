package com.pd.ecommerce.dto;

import com.pd.ecommerce.entity.Product;
import com.pd.ecommerce.entity.ProductByCategory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import java.time.Instant;
import java.util.UUID;

@Mapper(componentModel = "spring", imports = {UUID.class, Instant.class})
public interface ProductMapper {

	@Mapping(source = "productId", target = "id")
	@Mapping(target = "available", expression = "java(product.getActive() != null && product.getActive() && product.getStock() != null && product.getStock() > 0)")
	ProductResponse toResponse(Product product);

	@Mapping(target = "productId", expression = "java(UUID.randomUUID())")
	@Mapping(target = "createdAt", expression = "java(Instant.now())")
	@Mapping(target = "updatedAt", expression = "java(Instant.now())")
	@Mapping(target = "active", constant = "true")
	Product toEntity(ProductCreateRequest request);

	ProductResponse toEntity(ProductUpdateRequest request);

	@Mapping(source = "key.productId", target = "id")
	ProductByCategoryView toCategoryView(ProductByCategory product);
}