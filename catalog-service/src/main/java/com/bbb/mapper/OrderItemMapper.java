package com.bbb.mapper;

import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface OrderItemMapper {

//	@Mapping(target = "id", ignore = true)
//	@Mapping(target = "price", ignore = true) // if it's not part of request
//	@Mapping(target = "order", ignore = true) // will be set manually later
//	OrderItem toOrderItem(OrderItemRequest request);
}