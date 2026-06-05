package com.pd.ecommerce.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Slf4j
@Configuration
public class KafkaErrorConfig {

	@Bean
	public DefaultErrorHandler kafkaErrorHandler() {
		return new DefaultErrorHandler(
			(record, ex) -> log.error(
				"Skipping bad Kafka record: {}",
				record.value(),
				ex
			),
			new FixedBackOff(1000L, 5)
		);
	}
}