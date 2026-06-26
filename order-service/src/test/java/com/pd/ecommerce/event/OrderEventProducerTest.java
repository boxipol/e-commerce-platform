package com.pd.ecommerce.event;

import com.pd.ecommerce.kafka.OrderEventProducer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.stereotype.Component;
import org.springframework.test.context.TestPropertySource;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = OrderEventProducerTest.TestConfig.class)
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
	"spring.kafka.consumer.properties.spring.json.trusted.packages=*",
	"spring.kafka.consumer.auto-offset-reset=earliest"
})
class OrderEventProducerTest {

	@SpringBootConfiguration
	@EnableAutoConfiguration
	@Import({OrderEventProducer.class, TestListener.class})
	static class TestConfig {}

	@Component
	static class TestListener {
		private final CountDownLatch latch = new CountDownLatch(1);
		private volatile OrderCreatedEvent receivedEvent;

		@KafkaListener(topics = "order.created", groupId = "notification-group")
		void listen(OrderCreatedEvent event) {
			this.receivedEvent = event;
			latch.countDown();
		}
	}

	@Autowired
	private OrderEventProducer producer;

	@Autowired
	private TestListener listener;


	@Test
	void shouldPublishOrderCreatedEvent() throws Exception {
		OrderCreatedEvent event = OrderCreatedEvent.builder()
			.orderId(UUID.randomUUID())
			.userId(UUID.randomUUID()).items(List.of(
				new OrderItemEvent(UUID.randomUUID(), "SKU-001", 3),
				new OrderItemEvent(UUID.randomUUID(), "SKU-002", 2)))
			.totalPrice(BigDecimal.valueOf(199.99))
			.build();

		producer.publish(event);

		boolean consumed = listener.latch.await(10, TimeUnit.SECONDS);
		assertThat(consumed).isTrue();
		assertThat(listener.receivedEvent).isNotNull();
		assertThat(listener.receivedEvent.orderId()).isEqualTo(event.orderId());
	}
}