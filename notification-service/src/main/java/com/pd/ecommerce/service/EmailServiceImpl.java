package com.pd.ecommerce.service;

import com.pd.ecommerce.config.MailProperties;
import com.pd.ecommerce.event.OrderCreatedEvent;
import com.pd.ecommerce.event.PaymentCompletedEvent;
import com.pd.ecommerce.event.PaymentFailedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public final class EmailServiceImpl implements EmailService {

	private final JavaMailSender mailSender;
	private final MailProperties mailProperties;


	public void sendOrderCreatedEmail(OrderCreatedEvent event) {
		String subject = "Order Confirmation: " + event.orderId();

		try {
			SimpleMailMessage message = new SimpleMailMessage();
			message.setFrom(mailProperties.getFrom());
			message.setTo("petyo_dobrev@icloud.com");
			message.setSubject(subject);
			message.setText(buildOrderEmailBody(event));

			mailSender.send(message);
			log.info("Email sent for order {}", event.orderId());
		} catch (Exception ex) {
			log.error("Failed to send email for order {}", event.orderId(), ex);
			throw ex;
		}
	}

	@Override
	public void sendPaymentCompletedEmail(PaymentCompletedEvent event) {
		String subject = "Payment Confirmation: " + event.orderId();

		try {
			SimpleMailMessage message = new SimpleMailMessage();
			message.setFrom(mailProperties.getFrom());
			message.setTo("petyo_dobrev@icloud.com");
			message.setSubject(subject);
			message.setText(buildPaymentCompletedEmailBody(event));

			mailSender.send(message);
			log.info("Email sent for payment {}", event.orderId());
		} catch (Exception ex) {
			log.error("Failed to send email for payment {}", event.orderId(), ex);
			throw ex;
		}
	}

	@Override
	public void sendPaymentFailedEmail(PaymentFailedEvent event) {
		String subject = "Payment Confirmation: " + event.orderId();

		try {
			SimpleMailMessage message = new SimpleMailMessage();
			message.setFrom(mailProperties.getFrom());
			message.setTo("petyo_dobrev@icloud.com");
			message.setSubject(subject);
			message.setText(buildPaymentFailedEmailBody(event));

			mailSender.send(message);
			log.info("Email sent for payment {}", event.orderId());
		} catch (Exception ex) {
			log.error("Failed to send email for payment {}", event.orderId(), ex);
			throw ex;
		}
	}

//	==================== PRIVATE ====================

	private String buildOrderEmailBody(OrderCreatedEvent event) {
		return """
			Your order has been received!
			
			Order ID: %s
			Products: %s
			Total: %s
			
			Thank you for your purchase.
			""".formatted(event.orderId(), event.productIds(), event.totalPrice());
	}

	private String buildPaymentCompletedEmailBody(PaymentCompletedEvent event) {
		return """
			Your payment has been received!
			
			Order ID: %s
			Product ID: %s
			Amount: %s
			
			Thank you for your purchase.
			""".formatted(event.orderId(), event.paymentId(), event.amount());
	}

	private String buildPaymentFailedEmailBody(PaymentFailedEvent event) {
		return """
			Your payment has failed!
			
			Order ID: %s
			Product ID: %s
			Amount: %s
			
			Please try again!.
			""".formatted(event.orderId(), event.paymentId(), event.amount());
	}
}