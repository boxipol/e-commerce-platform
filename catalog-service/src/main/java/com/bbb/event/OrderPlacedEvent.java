package com.bbb.event;

public record OrderPlacedEvent(Long orderId, Long userId) {}