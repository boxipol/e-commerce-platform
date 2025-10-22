package com.pd.ecommerce.event;

public record OrderPlacedEvent(Long orderId, Long userId) {}