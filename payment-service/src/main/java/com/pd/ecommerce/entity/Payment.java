package com.pd.ecommerce.entity;

import com.pd.ecommerce.event.OrderItem;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Table("payments")
@Builder
@Getter
@Setter
public class Payment {

	@Id
	private UUID id;
	private UUID orderId;
	private UUID userId;
	private BigDecimal amount;
	private List<OrderItem> items;
	private String currency;
	private PaymentStatus status;
	private PaymentProvider provider;
	private String providerPaymentId;
	private String paymentUrl;
	private String failureReason;
	private Instant createdAt;
	private Instant updatedAt;
}