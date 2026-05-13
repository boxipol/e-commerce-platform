package com.pd.ecommerce.mapper;

import com.pd.ecommerce.dto.CreateOrderItemRequest;
import com.pd.ecommerce.dto.CreateOrderRequest;
import com.pd.ecommerce.dto.OrderItemResponse;
import com.pd.ecommerce.dto.OrderResponse;
import com.pd.ecommerce.dto.ProductSnapshot;
import com.pd.ecommerce.entity.Order;
import com.pd.ecommerce.entity.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Mapper(componentModel = "spring")
public interface OrderMapper {

	OrderItemResponse toItemResponse(OrderItem item);

	@Mapping(target = "id", source = "orderId")
	@Mapping(target = "userId", source = "request.userId")
	@Mapping(target = "status", constant = "CREATED")
	@Mapping(target = "totalAmount", source = "totalAmount")
	@Mapping(target = "createdAt", source = "createdAt")
	Order toOrder(CreateOrderRequest request, UUID orderId, BigDecimal totalAmount, Instant createdAt);

	default OrderResponse toResponse(Order order, List<OrderItem> items) {
		return OrderResponse.builder()
			.id(order.getId())
			.userId(order.getUserId())
			.status(order.getStatus())
			.totalAmount(order.getTotalAmount())
			.createdAt(order.getCreatedAt())
			.items(items.stream()
				.map(this::toItemResponse)
				.toList())
			.build();
	}

	@Mapping(target = "id", expression = "java(java.util.UUID.randomUUID())")
	@Mapping(target = "orderId", source = "orderId")
	@Mapping(target = "productId", source = "product.id")
	@Mapping(target = "quantity", source = "request.quantity")
	@Mapping(target = "unitPrice", source = "product.price")
	@Mapping(target = "subtotal", expression = """
		    java(
		        product.price().multiply(
		            java.math.BigDecimal.valueOf(request.quantity())
		        )
		    )
		""")
	OrderItem toOrderItem(CreateOrderItemRequest request, ProductSnapshot product, UUID orderId);
}