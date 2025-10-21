package com.bbb.mapper;

import com.bbb.dto.OrderItemRequest;
import com.bbb.entity.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderItemMapper {

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "price", ignore = true)
	@Mapping(target = "order", ignore = true)
	OrderItem toOrderItem(OrderItemRequest request);
}