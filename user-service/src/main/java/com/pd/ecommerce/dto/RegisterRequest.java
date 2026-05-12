package com.pd.ecommerce.dto;

public record RegisterRequest(
	String email,
	String password
) {}