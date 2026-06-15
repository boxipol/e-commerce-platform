package com.pd.ecommerce.converters;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pd.ecommerce.event.OrderEventItem;
import io.r2dbc.postgresql.codec.Json;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

import java.util.List;

@ReadingConverter
@RequiredArgsConstructor
public final class OrderItemsReadingConverter implements Converter<Json, List<OrderEventItem>> {

	private final ObjectMapper objectMapper;


	@Override
	public List<OrderEventItem> convert(Json source) {
		try {
			return objectMapper.readValue(source.asString(), new TypeReference<>() {});
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}
}