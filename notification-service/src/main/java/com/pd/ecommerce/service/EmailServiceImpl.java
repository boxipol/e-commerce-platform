package com.pd.ecommerce.service;

import com.pd.ecommerce.config.MailProperties;
import com.pd.ecommerce.event.OrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

	private final JavaMailSender mailSender;
	private final MailProperties mailProperties;


	public void sendOrderCreatedEmail(OrderCreatedEvent event) {
		String subject = "Order Confirmation: " + event.orderId();

		try {
			SimpleMailMessage message = new SimpleMailMessage();
			message.setFrom(mailProperties.getFrom());
			message.setTo("petyo_dobrev@icloud.com");
			message.setSubject(subject);
			message.setText(buildEmailBody(event));

			mailSender.send(message);
			log.info("Email sent for order {}", event.orderId());
		} catch (Exception ex) {
			log.error("Failed to send email for order {}", event.orderId(), ex);
			throw ex;
		}
	}

//	==================== PRIVATE ====================

	private String buildEmailBody(OrderCreatedEvent event) {
		return """
			Your order has been received!
			
			Order ID: %s
			Products: %s
			Total: %s
			
			Thank you for your purchase.
			""".formatted(event.orderId(), event.productIds(), event.totalPrice());
	}
}