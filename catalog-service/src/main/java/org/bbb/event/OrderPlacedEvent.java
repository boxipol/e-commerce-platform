package org.bbb.event;

public record OrderPlacedEvent(Long orderId, Long userId) {}