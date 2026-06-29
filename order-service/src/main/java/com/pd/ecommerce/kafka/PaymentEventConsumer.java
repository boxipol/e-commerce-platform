package com.pd.ecommerce.kafka;

import com.pd.ecommerce.event.PaymentFailedEvent;
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


	@KafkaListener(topics = "payment.failed", groupId = "order-group")
	public Mono<Void> onPaymentFailed(PaymentFailedEvent event) {
		return orderService.markAsFailed(event.orderId())
			.doOnSuccess(response ->
				log.info("Payment failed for order {}", event.orderId())
			);
	}
}