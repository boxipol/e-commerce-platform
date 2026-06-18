package com.pd.ecommerce.kafka;

import com.pd.ecommerce.event.PaymentCompletedEvent;
import com.pd.ecommerce.exception.InsufficientInventoryException;
import com.pd.ecommerce.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public final class PaymentEventConsumer {

	private final InventoryService service;
	private final InventoryEventProducer eventProducer;


	@KafkaListener(topics = "payment.completed", groupId = "inventory-group")
	public Mono<Void> onPaymentCompleted(PaymentCompletedEvent event) {
		return service.reserveInventory(event)
			.then(Mono.defer(() ->
				eventProducer.sendInventoryReserved(event.orderId())
			))
			.onErrorResume(InsufficientInventoryException.class, ex -> {
				log.error("Failed to reserve inventory for order {}", event.orderId(), ex);
				return eventProducer.sendInventoryFailed(event.orderId(), event.paymentId(), ex.getMessage());
			})
			.then();
	}
}
