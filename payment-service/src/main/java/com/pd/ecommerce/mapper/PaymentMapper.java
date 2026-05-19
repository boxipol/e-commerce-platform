package com.pd.ecommerce.mapper;

import com.pd.ecommerce.dto.CreatePaymentRequest;
import com.pd.ecommerce.dto.PaymentResponse;
import com.pd.ecommerce.entity.Payment;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PaymentMapper {

	PaymentResponse toResponse(Payment payment);

	Payment toEntity(CreatePaymentRequest request);
}