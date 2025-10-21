package com.pdp.ecommerce.event;

public record OrderPlacedEvent(Long orderId, Long userId) {}