package com.pd.ecommerce.service;

import com.pd.ecommerce.event.PaymentCompletedEvent;
import reactor.core.publisher.Mono;

public interface InventoryService {

	Mono<Void> reserveInventory(PaymentCompletedEvent event);
}