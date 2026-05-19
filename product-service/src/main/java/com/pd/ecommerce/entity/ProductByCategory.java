package com.pd.ecommerce.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;
import java.math.BigDecimal;

@Table("products_by_category")
@Builder
@Getter
@Setter
public final class ProductByCategory {

	@PrimaryKey
	private ProductByCategoryKey key;
	private String name;
	private String brand;
	private BigDecimal price;
	private Integer stock;
}