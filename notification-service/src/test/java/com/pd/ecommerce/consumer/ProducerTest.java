package com.pd.ecommerce.consumer;

import com.pd.ecommerce.event.OrderCreatedEvent;
import com.pd.ecommerce.event.OrderEventItem;
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
	private KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate;


	@Test
	void sendEventToRealKafka() {
		OrderCreatedEvent event = OrderCreatedEvent.builder()
			.orderId(UUID.randomUUID())
			.userId(UUID.randomUUID())
			.items(List.of(
				new OrderEventItem("UUID.randomUUID()", 3),
				new OrderEventItem("UUID.randomUUID()", 2)))
			.totalPrice(BigDecimal.valueOf(199.99))
			.build();

		kafkaTemplate.send("order.created", event);
	}
}