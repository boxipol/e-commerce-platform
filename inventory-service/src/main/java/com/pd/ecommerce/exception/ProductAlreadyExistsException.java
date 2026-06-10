package com.pd.ecommerce.exception;

import java.util.UUID;

public class ProductAlreadyExistsException extends RuntimeException {

	public ProductAlreadyExistsException(UUID productId) {
		super("Product already exists: " + productId);
	}
}