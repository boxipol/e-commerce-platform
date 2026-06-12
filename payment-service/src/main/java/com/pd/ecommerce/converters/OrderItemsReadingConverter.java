package com.pd.ecommerce.converters;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pd.ecommerce.event.OrderItem;
import io.r2dbc.postgresql.codec.Json;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

import java.util.List;

@ReadingConverter
@RequiredArgsConstructor
public final class OrderItemsReadingConverter implements Converter<Json, List<OrderItem>> {

	private final ObjectMapper objectMapper;


	@Override
	public List<OrderItem> convert(Json source) {
		try {
			return objectMapper.readValue(source.asString(), new TypeReference<>() {});
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}
}