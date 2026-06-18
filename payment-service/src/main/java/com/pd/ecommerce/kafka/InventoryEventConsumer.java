package com.pd.ecommerce.kafka;

import com.pd.ecommerce.event.InventoryReservationFailedEvent;
import com.pd.ecommerce.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
final class InventoryEventConsumer {

	private final PaymentService paymentService;


	@KafkaListener(topics = "reservation.failed", groupId = "payment-group")
	public Mono<Void> onReservationFailed(InventoryReservationFailedEvent event) {
		return paymentService.markForRefund(event.paymentId())
			.then();
	}
}
