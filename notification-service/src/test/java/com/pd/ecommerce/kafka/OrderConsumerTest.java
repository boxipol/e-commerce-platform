package com.pd.ecommerce.kafka;

import com.pd.ecommerce.event.OrderCreatedEvent;
import com.pd.ecommerce.event.OrderEventItem;
import com.pd.ecommerce.service.EmailService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderConsumer")
class OrderConsumerTest {

	@Mock
	private EmailService emailService;

	@InjectMocks
	private OrderConsumer consumer;


	private OrderCreatedEvent orderEvent() {
		return OrderCreatedEvent.builder().orderId(UUID.randomUUID()).publicOrderId("ORD-ABCD1234").userId(UUID.randomUUID()).userMail("customer@example.com").items(List.of(new OrderEventItem("SKU-001", 2))).totalPrice(new BigDecimal("199.98")).build();
	}

	@Test
	@DisplayName("consume(OrderCreatedEvent) - delegates to emailService.sendOrderCreatedEmail")
	void delegatesToEmailService() {
		OrderCreatedEvent event = orderEvent();
		consumer.consume(event);
		verify(emailService).sendOrderCreatedEmail(event);
	}

	@Test
	@DisplayName("consume(OrderCreatedEvent) - propagates exception from emailService")
	void propagatesEmailServiceException() {
		OrderCreatedEvent event = orderEvent();
		doThrow(new RuntimeException("SMTP error")).when(emailService).sendOrderCreatedEmail(event);
		org.assertj.core.api.Assertions.assertThatThrownBy(() -> consumer.consume(event)).isInstanceOf(RuntimeException.class).hasMessage("SMTP error");
	}
}