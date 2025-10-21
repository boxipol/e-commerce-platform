package com.pdp.ecommerce.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "orders")
@Builder
@Getter
@Setter
public final class Order {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private Long userId;

	@Enumerated(EnumType.STRING)
	private OrderStatus status;

	private Instant createdAt;

	@OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
	private List<OrderItem> items;
}