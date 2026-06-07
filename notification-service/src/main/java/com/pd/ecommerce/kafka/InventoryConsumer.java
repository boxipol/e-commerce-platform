package com.pd.ecommerce.kafka;

import com.pd.ecommerce.event.InventoryReservationCompletedEvent;
import com.pd.ecommerce.event.InventoryReservationFailedEvent;
import com.pd.ecommerce.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public final class InventoryConsumer {

	private final EmailService emailService;


	@KafkaListener(topics = "reservation.completed", groupId = "notification-group")
	public void consume(InventoryReservationCompletedEvent event) {
		log.info("Sending notification for completed reservation for order: {}", event.orderId());
		emailService.sendReservationCompletedEmail(event);
	}

	@KafkaListener(topics = "reservation.failed", groupId = "notification-group")
	public void consume(InventoryReservationFailedEvent event) {
		log.info("Sending notification for failed reservation for order: {}", event.orderId());
		emailService.sendReservationFailedEmail(event);
	}
}