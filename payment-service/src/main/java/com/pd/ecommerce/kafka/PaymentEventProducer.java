package com.pd.ecommerce.kafka;

import com.pd.ecommerce.entity.Payment;
import com.pd.ecommerce.event.PaymentCompletedEvent;
import com.pd.ecommerce.event.PaymentFailedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public final class PaymentEventProducer {

	private final KafkaTemplate<String, Object> kafkaTemplate;


	public void sendPaymentCompleted(Payment payment) {
		PaymentCompletedEvent event = new PaymentCompletedEvent(
			payment.getId(),
			payment.getOrderId(),
			payment.getAmount()
		);

		kafkaTemplate.send(
			"payment.completed",
			payment.getOrderId().toString(),
			event
		);
	}

	public void sendPaymentFailed(Payment payment) {
		PaymentFailedEvent event = new PaymentFailedEvent(
			payment.getId(),
			payment.getOrderId(),
			payment.getAmount()
		);

		kafkaTemplate.send(
			"payment.failed",
			payment.getOrderId().toString(),
			event
		);
	}
}