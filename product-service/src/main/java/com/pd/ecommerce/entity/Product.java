package com.pd.ecommerce.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;
import java.math.BigDecimal;
import java.util.UUID;

@Table(value = "product")
@Getter
@Setter
public class Product {

	@PrimaryKey
	private UUID id;
	private String name;
	private String description;
	private BigDecimal price;
}