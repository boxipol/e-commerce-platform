package com.pd.ecommerce.dto;

public record LoginRequest(
	String email,
	String password
) {}