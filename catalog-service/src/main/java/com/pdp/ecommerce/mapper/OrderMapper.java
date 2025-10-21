package com.pdp.ecommerce.mapper;

import com.pdp.ecommerce.dto.OrderRequest;
import com.pdp.ecommerce.dto.OrderResponse;
import com.pdp.ecommerce.entity.Order;
import com.pdp.ecommerce.entity.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderMapper {

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "status", constant = "PENDING")
	@Mapping(target = "createdAt", expression = "java(Instant.now())")
	Order toOrder(OrderRequest request);

	/**
	 * Helper method to set the back-reference after mapping.
	 * MapStruct can't automatically do bidirectional relations.
	 */
//	@AfterMapping
//	default void linkOrderItems(@MappingTarget Order order) {
//		if (order.getItems() != null) {
//			order.getItems()
//				.forEach(item -> item.setOrder(order));
//		}
//	}

	// DTO -> Entity

//	@Mapping(target = "items", source = "items")
//	Order toEntity(OrderRequest request);

	// Entity -> DTO
	@Mapping(target = "status", expression = "java(order.getStatus().name())")
	OrderResponse toResponse(Order order);

	// Nested mappings
//	OrderItem toEntity(OrderItemRequest request);

	OrderResponse toResponse(OrderItem item);


}