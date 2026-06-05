package com.pd.ecommerce.kafka;

import com.pd.ecommerce.event.PaymentCompletedEvent;
import com.pd.ecommerce.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public final class PaymentEventConsumer {

	private final OrderService orderService;


	@KafkaListener(topics = "payment.completed")
	public Mono<Void> onPaymentCompleted(PaymentCompletedEvent event) {
		return orderService.markAsPaid(event.orderId())
			.doOnSuccess(response ->
				log.info("Payment completed for order {}", event.orderId())
			)
			.doOnError(error ->
				log.error("Failed completing payment for order {}", event.orderId(), error)
			);
	}
}
