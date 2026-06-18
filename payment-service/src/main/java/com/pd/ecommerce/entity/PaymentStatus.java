package com.pd.ecommerce.entity;

public enum PaymentStatus {

	PENDING,
	REQUIRES_ACTION,
	PROCESSING,
	COMPLETED,
	FAILED,
	REFUNDING,
	REFUNDED,
	CANCELLED
}