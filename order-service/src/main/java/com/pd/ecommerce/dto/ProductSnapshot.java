package com.pd.ecommerce.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductSnapshot(
	UUID id,
	BigDecimal price
) {}