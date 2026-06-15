package com.pd.ecommerce.kafka;

import com.pd.ecommerce.entity.Payment;
import com.pd.ecommerce.event.PaymentCompletedEvent;
import com.pd.ecommerce.event.PaymentFailedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public final class PaymentEventProducer {

	private static final String COMPLETED_TOPIC = "payment.completed";
	private static final String FAILED_TOPIC = "payment.failed";

	private final KafkaTemplate<String, Object> kafkaTemplate;


	public Mono<Void> sendPaymentCompleted(Payment payment) {
		PaymentCompletedEvent event = PaymentCompletedEvent.builder()
			.paymentId(payment.getId())
			.userMail(payment.getUserMail())
			.orderId(payment.getOrderId())
			.publicOrderId(payment.getPublicOrderId())
			.items(payment.getItems())
			.amount(payment.getAmount())
			.build();

		return Mono.fromFuture(
				kafkaTemplate.send(
					COMPLETED_TOPIC,
					payment.getOrderId().toString(),
					event
				)
			)
			.doOnSuccess(result ->
				log.info(
					"Published PaymentCompletedEvent paymentId={}, orderId={}",
					payment.getId(),
					payment.getOrderId()
				)
			)
			.then();
	}

	public Mono<Void> sendPaymentFailed(Payment payment) {
		PaymentFailedEvent event = PaymentFailedEvent.builder()
			.paymentId(payment.getId())
			.userMail(payment.getUserMail())
			.orderId(payment.getOrderId())
			.publicOrderId(payment.getPublicOrderId())
			.amount(payment.getAmount())
			.build();

		return Mono.fromFuture(
				kafkaTemplate.send(
					FAILED_TOPIC,
					payment.getOrderId().toString(),
					event
				)
			)
			.doOnSuccess(result ->
				log.info(
					"Published PaymentFailedEvent paymentId={}, orderId={}",
					payment.getId(),
					payment.getOrderId()
				)
			)
			.then();
	}
}