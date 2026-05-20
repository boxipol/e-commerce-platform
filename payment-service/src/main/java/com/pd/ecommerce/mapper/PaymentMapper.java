package com.pd.ecommerce.mapper;

import com.pd.ecommerce.dto.CreatePaymentRequest;
import com.pd.ecommerce.dto.PaymentResponse;
import com.pd.ecommerce.entity.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PaymentMapper {

	PaymentResponse toResponse(Payment payment);

	@Mapping(target = "status", ignore = true)
	Payment toEntity(CreatePaymentRequest request);
}