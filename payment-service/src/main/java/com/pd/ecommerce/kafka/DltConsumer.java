package com.pd.ecommerce.kafka;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DltConsumer {

	@KafkaListener(
		topics = {"order.created.DLT", "reservation.failed.DLT"},
		groupId = "payment-dlt-group"
	)
	public void onDeadLetter(
		ConsumerRecord<String, Object> record,
		@Header(value = KafkaHeaders.DLT_EXCEPTION_FQCN, required = false) byte[] exFqcn,
		@Header(value = KafkaHeaders.DLT_EXCEPTION_MESSAGE, required = false) byte[] exMessage,
		@Header(value = KafkaHeaders.DLT_ORIGINAL_TOPIC, required = false) byte[] originalTopic
	){
		log.error(
			"[DLT] payment-service — topic={} partition={} offset={} | original-topic={} | exception={} | message={}",
			record.topic(),
			record.partition(),
			record.offset(),
			originalTopic != null ? new String(originalTopic) : "unknown",
			exFqcn != null ? new String(exFqcn) : "unknown",
			exMessage != null ? new String(exMessage) : "unknown"
		);
	}
}