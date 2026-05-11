package com.pd.ecommerce.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyClass;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import java.time.Instant;
import java.util.UUID;

@PrimaryKeyClass
@Getter
@Setter
public final class ProductByCategoryKey {

	@PrimaryKeyColumn(name = "category", type = PrimaryKeyType.PARTITIONED)
	private String category;

	@PrimaryKeyColumn(name = "created_at", type = PrimaryKeyType.CLUSTERED)
	private Instant createdAt;

	@PrimaryKeyColumn(name = "product_id", type = PrimaryKeyType.CLUSTERED)
	private UUID productId;
}