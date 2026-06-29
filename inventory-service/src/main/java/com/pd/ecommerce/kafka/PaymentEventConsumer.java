package com.pd.ecommerce.kafka;

import com.pd.ecommerce.event.InventoryReservationFailedEvent;
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
				eventProducer.sendInventoryReserved(event.orderId(), event.items())
			))
			.onErrorResume(InsufficientInventoryException.class, ex -> {
				log.error("Failed to reserve inventory for order {}", event.orderId(), ex);

				InventoryReservationFailedEvent failedEvent = InventoryReservationFailedEvent.builder()
					.orderId(event.orderId())
					.publicOrderId(event.publicOrderId())
					.paymentId(event.paymentId())
					.userMail(event.userMail())
					.reason(ex.getMessage())
					.build();

				return eventProducer.sendInventoryFailed(failedEvent);
			})
			.then();
	}
}
