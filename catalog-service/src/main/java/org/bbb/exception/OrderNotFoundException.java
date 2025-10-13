package org.bbb.exception;

public class OrderNotFoundException extends RuntimeException {

	public OrderNotFoundException(Long orderId) {
		super("Order with ID " + orderId + " was not found");
	}
}