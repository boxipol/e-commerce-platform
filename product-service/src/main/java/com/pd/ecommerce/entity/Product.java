package com.pd.ecommerce.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Table("products_by_id")
@Getter
@Setter
public final class Product {

	@PrimaryKey
	@Column("product_id")
	private UUID productId;

	private String sku;
	private String name;
	private String description;
	private String brand;
	private String category;
	private BigDecimal price;
	private String currency;
	private Integer stock;
	private Boolean active;

	@Column("created_at")
	private Instant createdAt;

	@Column("updated_at")
	private Instant updatedAt;
}