package com.pd.ecommerce.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pd.ecommerce.entity.OutboxEvent;
import com.pd.ecommerce.entity.OutboxEventStatus;
import com.pd.ecommerce.event.OrderCreatedEvent;
import com.pd.ecommerce.repository.OutboxEventRepository;
import io.r2dbc.postgresql.codec.Json;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OutboxPublisher Tests")
class OutboxPublisherTest {

	@Mock
	private OutboxEventRepository outboxRepository;

	@Mock
	private KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate;

	@Mock
	private ObjectMapper objectMapper;

	@InjectMocks
	private OutboxPublisher publisher;

	private OutboxEvent pendingEvent;
	private OrderCreatedEvent orderEvent;


	@BeforeEach
	void setUp() {
		String payload = "{\"orderId\":\"x\"}";
		pendingEvent = OutboxEvent.builder()
			.id(UUID.randomUUID())
			.aggregateId(UUID.randomUUID())
			.eventType("order.created")
			.payload(Json.of(payload))
			.status(OutboxEventStatus.PENDING)
			.createdAt(Instant.now())
			.build();
		orderEvent = OrderCreatedEvent.builder()
			.orderId(UUID.randomUUID())
			.publicOrderId("ORD-1")
			.userId(UUID.randomUUID())
			.userMail("buyer@example.com")
			.items(List.of())
			.totalPrice(BigDecimal.TEN)
			.build();
	}

	@Test
	@DisplayName("publishOutboxEvents - should send pending events to kafka and persist status updates")
	void testPublishPendingEvents() throws Exception {
		when(outboxRepository.findByStatus(OutboxEventStatus.PENDING)).thenReturn(Flux.just(pendingEvent));
		when(outboxRepository.save(any(OutboxEvent.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));
		when(objectMapper.readValue(any(String.class), eq(OrderCreatedEvent.class))).thenReturn(orderEvent);
		when(kafkaTemplate.send(eq("order.created"), eq(orderEvent)))
			.thenReturn(CompletableFuture.completedFuture(sendResult()));

		publisher.publishOutboxEvents();

		await().untilAsserted(() ->
			verify(kafkaTemplate).send("order.created", orderEvent));

		ArgumentCaptor<OutboxEvent> captor = ArgumentCaptor.forClass(OutboxEvent.class);
		verify(outboxRepository, atLeastOnce()).save(captor.capture());
		assertThat(captor.getAllValues())
			.allMatch(e -> e.getStatus() == OutboxEventStatus.PROCESSING);
	}

	@Test
	@DisplayName("publishOutboxEvents - should do nothing when there are no pending events")
	void testNoPendingEvents() {
		when(outboxRepository.findByStatus(OutboxEventStatus.PENDING)).thenReturn(Flux.empty());

		publisher.publishOutboxEvents();

		verify(outboxRepository).findByStatus(OutboxEventStatus.PENDING);
	}

	@SuppressWarnings("unchecked")
	private SendResult<String, OrderCreatedEvent> sendResult() {
		return (SendResult<String, OrderCreatedEvent>) org.mockito.Mockito.mock(SendResult.class);
	}
}