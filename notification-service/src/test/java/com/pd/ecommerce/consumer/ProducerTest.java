package com.pd.ecommerce.consumer;

import com.pd.ecommerce.event.OrderCreatedEvent;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@SpringBootTest
class ProducerTest {

	@Autowired
	KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate;


	@Test
	void sendEventToRealKafka() {
		OrderCreatedEvent event = OrderCreatedEvent.builder()
			.orderId(UUID.randomUUID())
			.userId(UUID.randomUUID())
			.productIds(List.of("p1", "p2"))
			.totalPrice(BigDecimal.valueOf(199.99))
			.build();

		kafkaTemplate.send("order.created", event);
	}
}