package com.pd.ecommerce.kafka;

import com.pd.ecommerce.event.OrderCreatedEvent;
import com.pd.ecommerce.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public final class OrderCreatedConsumer {

	private final PaymentService paymentService;


	@KafkaListener(topics = "order.created", groupId = "payment-group")
	public void consume(OrderCreatedEvent event) {
		paymentService.createPayment(event)
			.doOnSuccess(response ->
				log.info("Payment created for order {}", event.orderId())
			)
			.doOnError(error ->
				log.error("Failed creating payment for order {}", event.orderId(), error)
			)
			.subscribe();
	}
}