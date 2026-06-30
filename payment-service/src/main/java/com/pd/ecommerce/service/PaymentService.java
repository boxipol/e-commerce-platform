package com.pd.ecommerce.service;

import com.pd.ecommerce.dto.PaymentResponse;
import com.pd.ecommerce.event.OrderCreatedEvent;
import reactor.core.publisher.Mono;
import java.util.UUID;

public interface PaymentService {

	Mono<PaymentResponse> createPayment(OrderCreatedEvent event);
	Mono<PaymentResponse> getById(UUID id);
	Mono<PaymentResponse> getByOrderId(UUID orderId);
	Mono<Void> markForRefund(UUID paymentId);
}