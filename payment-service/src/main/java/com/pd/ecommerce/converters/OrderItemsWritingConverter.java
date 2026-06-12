package com.pd.ecommerce.converters;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pd.ecommerce.event.OrderItem;
import io.r2dbc.postgresql.codec.Json;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;

import java.util.List;

@WritingConverter
@RequiredArgsConstructor
public final class OrderItemsWritingConverter implements Converter<List<OrderItem>, Json> {

	private final ObjectMapper objectMapper;


	@Override
	public Json convert(List<OrderItem> source) {
		try {
			return Json.of(objectMapper.writeValueAsString(source));
		} catch (JsonProcessingException e) {
			throw new IllegalStateException(e);
		}
	}
}