package com.pd.ecommerce.exception;

public class OrderNotFoundException extends RuntimeException {

	public OrderNotFoundException(String publicOrderId) {
		super("Order with ID " + publicOrderId + " was not found");
	}
}