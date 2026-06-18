package com.pd.ecommerce.entity;

import io.r2dbc.postgresql.codec.Json;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("outbox_events")
public final class OutboxEvent {

	@Id
	private UUID id;
	private String aggregateType;
	private UUID aggregateId;
	private String eventType;
	/**
	 * JSON serialized event payload
	 */
	@Column("payload")
	private Json payload;
	/**
	 * PENDING
	 * PUBLISHED
	 * FAILED
	 */
	private OutboxEventStatus status;
	private Instant createdAt;
	private Instant publishedAt;
}