package com.pd.ecommerce.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pd.ecommerce.converters.OrderItemsReadingConverter;
import com.pd.ecommerce.converters.OrderItemsWritingConverter;
import io.r2dbc.spi.ConnectionFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.r2dbc.convert.R2dbcCustomConversions;
import org.springframework.data.r2dbc.dialect.DialectResolver;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;
import java.util.List;

@Configuration
@EnableR2dbcRepositories
@RequiredArgsConstructor
public class R2dbcConfig {

	private final ObjectMapper objectMapper;


	@Bean
	public R2dbcCustomConversions r2dbcCustomConversions(ConnectionFactory connectionFactory) {
		var dialect = DialectResolver.getDialect(connectionFactory);

		return R2dbcCustomConversions.of(
			dialect,
			List.of(
				new OrderItemsWritingConverter(objectMapper),
				new OrderItemsReadingConverter(objectMapper)
			)
		);
	}
}