package com.pd.ecommerce.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import java.time.Instant;
import java.util.UUID;

@Table("inventory")
@Builder
@Getter
@Setter
public final class Inventory {

	@Id
	UUID productId;
	Integer quantity;
	Instant createdAt;
	Instant updatedAt;
}