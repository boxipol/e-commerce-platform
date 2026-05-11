package com.pd.ecommerce.dto;

import com.datastax.oss.driver.api.core.cql.Row;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProductRowMapper {

	@Mapping(target = "id", expression = "java(row.getUuid(\"product_id\"))")
	@Mapping(target = "name", expression = "java(row.getString(\"name\"))")
	@Mapping(target = "description", expression = "java(row.getString(\"description\"))")
	@Mapping(target = "brand", expression = "java(row.getString(\"brand\"))")
	@Mapping(target = "category", expression = "java(row.getString(\"category\"))")
	@Mapping(target = "price", expression = "java(row.getBigDecimal(\"price\"))")
	@Mapping(target = "currency", expression = "java(row.getString(\"currency\"))")
	@Mapping(target = "available", expression = """
		    java(
		        Boolean.TRUE.equals(row.getBoolean("active"))
		        && row.getInt("stock") > 0
		    )
		""")
	@Mapping(target = "createdAt", expression = "java(row.getInstant(\"created_at\"))")
	ProductResponse toResponse(Row row);
}