package com.pd.ecommerce.entity;

import lombok.Getter;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Table("products_by_sku")
@Getter
public class ProductBySku {

	@PrimaryKey
	private String sku;

	private UUID productId;

	private String name;
	private String description;

	private String brand;
	private String category;

	private BigDecimal price;
	private String currency;

	private Integer stock;
	private Boolean active;

	private Instant createdAt;
	private Instant updatedAt;
}