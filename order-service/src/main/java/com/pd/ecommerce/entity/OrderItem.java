package com.pd.ecommerce.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import java.math.BigDecimal;
import java.util.UUID;

@Table("order_items")
@Builder
@Getter
@Setter
public final class OrderItem {

	@Id
	private UUID id;
	private UUID orderId;
	private UUID productId;
	private Integer quantity;
	private BigDecimal unitPrice;
	private BigDecimal subtotal;
}