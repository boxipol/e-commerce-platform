package com.pd.ecommerce.consumer;

import com.pd.ecommerce.event.PaymentCompletedEvent;
import com.pd.ecommerce.event.PaymentFailedEvent;
import com.pd.ecommerce.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public final class PaymentCreatedConsumer {

	private final EmailService emailService;


	@KafkaListener(topics = "payment.completed", groupId = "notification-group")
	public void consume(PaymentCompletedEvent event) {
		log.info("Received payment completed event: {}", event);
		log.info("Sending notification for payment {}", event.orderId());

		emailService.sendPaymentCompletedEmail(event);
	}

	@KafkaListener(topics = "payment.failed", groupId = "notification-group")
	public void consume(PaymentFailedEvent event) {
		log.info("Received payment failed event: {}", event);
		log.info("Sending notification for payment {}", event.orderId());

		emailService.sendPaymentFailedEmail(event);
	}
}