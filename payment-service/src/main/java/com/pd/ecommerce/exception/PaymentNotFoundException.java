package com.pd.ecommerce.exception;

import lombok.Getter;

@Getter
public class PaymentNotFoundException extends RuntimeException {

	private final String providerPaymentId;


	public PaymentNotFoundException(String providerPaymentId) {
		super("Payment not found for providerPaymentId=" + providerPaymentId);
		this.providerPaymentId = providerPaymentId;
	}
}