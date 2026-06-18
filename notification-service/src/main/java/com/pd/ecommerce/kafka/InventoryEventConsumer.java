package com.pd.ecommerce.kafka;

import com.pd.ecommerce.event.InventoryReservationFailedEvent;
import com.pd.ecommerce.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
final class InventoryEventConsumer {

	private final EmailService emailService;


	@KafkaListener(topics = "reservation.failed", groupId = "notification-group")
	public void onReservationFailed(InventoryReservationFailedEvent event) {
		log.info("Received reservation failed event: {}", event);
		log.info("Sending notification for reservation {}", event.orderId());

		emailService.sendReservationFailedEmail(event);
	}
}
