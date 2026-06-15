package com.pd.ecommerce.mapper;

import com.pd.ecommerce.dto.ProductCreateRequest;
import com.pd.ecommerce.dto.ProductResponse;
import com.pd.ecommerce.entity.Product;
import com.pd.ecommerce.entity.ProductByCategory;
import com.pd.ecommerce.entity.ProductByCategoryKey;
import com.pd.ecommerce.entity.ProductBySku;
import com.pd.ecommerce.util.SkuGenerator;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import java.time.Instant;
import java.util.UUID;

@Mapper(componentModel = "spring", imports = {UUID.class, Instant.class})
public interface ProductMapper {

	@Mapping(target = "available", expression = "java(product.getActive() != null && product.getActive() && product.getStock() != null && product.getStock() > 0)")
	ProductResponse toResponse(Product product);

	@Mapping(target = "available", expression = "java(productBySku.getActive() != null && productBySku.getActive() && productBySku.getStock() != null && productBySku.getStock() > 0)")
	ProductResponse toResponse(ProductBySku productBySku);

	@Mapping(target = "productId", expression = "java(UUID.randomUUID())")
	@Mapping(target = "createdAt", expression = "java(Instant.now())")
	@Mapping(target = "updatedAt", expression = "java(Instant.now())")
	@Mapping(target = "active", constant = "true")
	Product toProduct(ProductCreateRequest request);

	@Mapping(target = "sku", expression = "java(generateSku(request))")
	@Mapping(target = "productId", expression = "java(UUID.randomUUID())")
	@Mapping(target = "createdAt", expression = "java(Instant.now())")
	@Mapping(target = "updatedAt", expression = "java(Instant.now())")
	@Mapping(target = "active", constant = "true")
	ProductBySku toProductBySku(ProductCreateRequest request);

	default String generateSku(ProductCreateRequest request) {
		return SkuGenerator.generate(request.brand(), request.name(), request.description());
	}

	default ProductByCategory toProductByCategoryView(Product product) {
		return ProductByCategory.builder()
			.key(
				ProductByCategoryKey.builder()
					.category(product.getCategory())
					.createdAt(product.getCreatedAt())
					.sku(product.getSku())
					.build()
			)

			.name(product.getName())
			.brand(product.getBrand())
			.price(product.getPrice())
			.stock(product.getStock())
			.build();
	}
}