package com.pd.ecommerce.consumer;

import com.pd.ecommerce.event.OrderCreatedEvent;
import com.pd.ecommerce.event.OrderEventItem;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.TestPropertySource;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@SpringBootTest
@EmbeddedKafka(partitions = 1, topics = {"order.created"})
@TestPropertySource(properties = {
	"spring.kafka.bootstrap-servers=localhost:29092",
	"spring.kafka.producer.key-serializer=org.apache.kafka.common.serialization.StringSerializer",
	"spring.kafka.producer.value-serializer=org.springframework.kafka.support.serializer.JsonSerializer",
	"spring.kafka.consumer.key-deserializer=org.apache.kafka.common.serialization.StringDeserializer",
	"spring.kafka.consumer.value-deserializer=org.springframework.kafka.support.serializer.JsonDeserializer",
	"spring.kafka.consumer.properties.spring.json.trusted.packages=*"
})
public class OrderCreatedConsumerTest {

	@Autowired
	private KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate;


	@Test
	void shouldConsumeEvent() throws Exception {
		OrderCreatedEvent event = OrderCreatedEvent.builder()
			.orderId(UUID.randomUUID())
			.userId(UUID.randomUUID())
			.items(List.of(
				new OrderEventItem("UUID.randomUUID()", 3),
				new OrderEventItem("UUID.randomUUID()", 2)))
			.totalPrice(BigDecimal.valueOf(199.99))
			.build();

//		kafkaTemplate.send("order.created", event);
//		Thread.sleep(2000); // simple sync wait for demo
	}
}