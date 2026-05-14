package com.pd.ecommerce.consumer;

import com.pd.ecommerce.event.OrderCreatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public final class OrderCreatedConsumer {

	@KafkaListener(topics = "order.created", groupId = "notification-group")
	public void consume(OrderCreatedEvent event) {
		log.info("Received order created event: {}", event);
		log.info("Sending notification for order {}", event.orderId());
	}
}