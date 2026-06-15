package com.pd.ecommerce.mapper;

import com.pd.ecommerce.dto.OrderItemResponse;
import com.pd.ecommerce.dto.OrderResponse;
import com.pd.ecommerce.entity.Order;
import com.pd.ecommerce.entity.OrderItem;
import com.pd.ecommerce.entity.OrderStatus;
import com.pd.ecommerce.event.OrderCreatedEvent;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import java.util.List;

@Mapper(componentModel = "spring", imports = OrderStatus.class)
public interface OrderMapper {

	OrderItemResponse toItemResponse(OrderItem item);

	default OrderResponse toResponse(String userMail, Order order, List<OrderItem> items) {
		return OrderResponse.builder()
			.publicOrderId(order.getPublicOrderId())
			.userMail(userMail)
			.status(order.getStatus())
			.totalAmount(order.getTotalAmount())
			.createdAt(order.getCreatedAt())
			.items(items.stream()
				.map(this::toItemResponse)
				.toList())
			.build();
	}

	@Mapping(target = "orderId", source = "order.id")
	@Mapping(target = "publicOrderId", source = "order.publicOrderId")
	@Mapping(target = "userId", source = "order.userId")
	@Mapping(target = "userMail", source = "order.userMail")
	@Mapping(target = "totalPrice", source = "order.totalAmount")
	OrderCreatedEvent toOrderCreatedEvent(Order order, List<OrderItem> items);
}