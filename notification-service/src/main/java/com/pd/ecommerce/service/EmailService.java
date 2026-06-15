package com.pd.ecommerce.service;

import com.pd.ecommerce.event.OrderCreatedEvent;
import com.pd.ecommerce.event.PaymentCompletedEvent;
import com.pd.ecommerce.event.PaymentFailedEvent;
import com.pd.ecommerce.event.UserCreatedEvent;
import com.pd.ecommerce.event.UserDeletedEvent;

public interface EmailService {

	void sendOrderCreatedEmail(OrderCreatedEvent event);
	void sendPaymentCompletedEmail(PaymentCompletedEvent event);
	void sendPaymentFailedEmail(PaymentFailedEvent event);
	void sendUserCreatedEmail(UserCreatedEvent event);
	void sendUserDeletedEmail(UserDeletedEvent event);
}