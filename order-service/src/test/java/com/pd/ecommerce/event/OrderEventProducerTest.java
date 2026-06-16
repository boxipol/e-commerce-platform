package com.pd.ecommerce.event;

import com.pd.ecommerce.kafka.OrderEventProducer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.TestPropertySource;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@EmbeddedKafka(
	partitions = 1,
	topics = {"order.created"},
	brokerProperties = {"auto.create.topics.enable=true"}
)
@TestPropertySource(properties = {
	"spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
	"spring.kafka.producer.key-serializer=org.apache.kafka.common.serialization.StringSerializer",
	"spring.kafka.producer.value-serializer=org.springframework.kafka.support.serializer.JsonSerializer",
	"spring.kafka.consumer.key-deserializer=org.apache.kafka.common.serialization.StringDeserializer",
	"spring.kafka.consumer.value-deserializer=org.springframework.kafka.support.serializer.JsonDeserializer",
	"spring.kafka.consumer.properties.spring.json.trusted.packages=*"
})
class OrderEventProducerTest {

	@Autowired
	private OrderEventProducer producer;

	private final CountDownLatch latch = new CountDownLatch(1);

	private OrderCreatedEvent receivedEvent;


	@Test
	void shouldPublishOrderCreatedEvent() throws Exception {
		OrderCreatedEvent event = OrderCreatedEvent.builder()
			.orderId(UUID.randomUUID())
			.userId(UUID.randomUUID()).items(List.of(
				new OrderItemEvent("SKU-001", 3),
				new OrderItemEvent("SKU-002", 2)))
			.totalPrice(BigDecimal.valueOf(199.99))
			.build();

		producer.publish(event);

		boolean consumed = latch.await(10, TimeUnit.SECONDS);
		assertThat(consumed).isTrue();
		assertThat(receivedEvent).isNotNull();
		assertThat(receivedEvent.orderId()).isEqualTo(event.orderId());
	}

	@KafkaListener(topics = "order.created", groupId = "notification-group")
	void listen(OrderCreatedEvent event) {
		this.receivedEvent = event;
		latch.countDown();
	}
}