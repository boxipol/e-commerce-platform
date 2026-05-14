package com.pd.ecommerce.service;

import com.pd.ecommerce.event.OrderCreatedEvent;

public interface EmailService {

	void sendOrderCreatedEmail(OrderCreatedEvent event);
}
