package com.pd.ecommerce.service;

import com.pd.ecommerce.dto.PaymentResponse;
import com.pd.ecommerce.event.OrderCreatedEvent;
import reactor.core.publisher.Mono;

public interface PaymentService {

	Mono<PaymentResponse> createPayment(OrderCreatedEvent event);
}