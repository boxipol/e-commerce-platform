package com.pd.ecommerce.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Table;
import java.time.Instant;
import java.util.UUID;

@Table("inventory")
@Builder
@Getter
@Setter
public final class Inventory {

	@Id
	private UUID productId;
	private int stock;
	private Instant updatedAt;

	@Version
	private Long version;
}