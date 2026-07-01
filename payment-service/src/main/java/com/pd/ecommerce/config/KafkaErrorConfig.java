package com.pd.ecommerce.config;

import org.apache.kafka.common.TopicPartition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.ExponentialBackOffWithMaxRetries;
import org.springframework.kafka.support.serializer.DeserializationException;

@Configuration
public class KafkaErrorConfig {

	@Bean
	public DefaultErrorHandler kafkaErrorHandler(KafkaTemplate<String, Object> kafkaTemplate) {
		var recoverer = new DeadLetterPublishingRecoverer(kafkaTemplate,
			(record, ex) -> new TopicPartition(record.topic() + ".DLT", record.partition()));

		var backoff = new ExponentialBackOffWithMaxRetries(3);
		backoff.setInitialInterval(1_000L);
		backoff.setMultiplier(2.0);
		backoff.setMaxInterval(10_000L);

		var handler = new DefaultErrorHandler(recoverer, backoff);
		handler.addNotRetryableExceptions(DeserializationException.class);

		return handler;
	}
}