package com.pd.ecommerce.mapper;

import com.pd.ecommerce.dto.InventoryCreateRequest;
import com.pd.ecommerce.dto.InventoryResponse;
import com.pd.ecommerce.entity.Inventory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface InventoryMapper {

	InventoryResponse toResponse(Inventory inventory);

	@Mapping(target = "createdAt", expression = "java(java.time.Instant.now())")
	@Mapping(target = "updatedAt", expression = "java(java.time.Instant.now())")
	Inventory toEntity(InventoryCreateRequest request);
}