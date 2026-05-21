package com.pd.ecommerce.entity;

public enum PaymentStatus {

	PENDING,
	REQUIRES_ACTION,
	PROCESSING,
	SUCCEEDED,
	FAILED,
	REFUNDED,
	CANCELLED
}