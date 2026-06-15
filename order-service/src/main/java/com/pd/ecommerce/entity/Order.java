package com.pd.ecommerce.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Table("orders")
@Builder
@Getter
@Setter
public final class Order {

	@Id
	private UUID id;
	private String publicOrderId;
	private UUID userId;
	private String userMail;
	private OrderStatus status;
	private BigDecimal totalAmount;
	private Instant createdAt;
}