package com.pd.ecommerce.service;

import com.pd.ecommerce.config.MailProperties;
import com.pd.ecommerce.event.InventoryReservationCompletedEvent;
import com.pd.ecommerce.event.InventoryReservationFailedEvent;
import com.pd.ecommerce.event.OrderCreatedEvent;
import com.pd.ecommerce.event.PaymentCompletedEvent;
import com.pd.ecommerce.event.PaymentFailedEvent;
import com.pd.ecommerce.event.UserCreatedEvent;
import com.pd.ecommerce.event.UserDeletedEvent;
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

	@Override
	public void sendReservationCompletedEmail(InventoryReservationCompletedEvent event) {
		String subject = "Reservation Confirmation: " + event.orderId();

		try {
			SimpleMailMessage message = new SimpleMailMessage();
			message.setFrom(mailProperties.getFrom());
			message.setTo("petyo_dobrev@icloud.com");
			message.setSubject(subject);
			message.setText(buildReservationCompletedEmailBody(event));

			mailSender.send(message);
			log.info("Email sent for reservation {}", event.orderId());
		} catch (Exception ex) {
			log.error("Failed to send email for reservation {}", event.orderId(), ex);
			throw ex;
		}
	}

	@Override
	public void sendReservationFailedEmail(InventoryReservationFailedEvent event) {
		String subject = "Payment Confirmation: " + event.orderId();

		try {
			SimpleMailMessage message = new SimpleMailMessage();
			message.setFrom(mailProperties.getFrom());
			message.setTo("petyo_dobrev@icloud.com");
			message.setSubject(subject);
			message.setText(buildReservationFailedEmailBody(event));

			mailSender.send(message);
			log.info("Email sent for reservation {}", event.orderId());
		} catch (Exception ex) {
			log.error("Failed to send email for reservation {}", event.orderId(), ex);
			throw ex;
		}
	}

	@Override
	public void sendUserCreatedEmail(UserCreatedEvent event) {
		String subject = "User Confirmation: " + event.email();

		try {
			SimpleMailMessage message = new SimpleMailMessage();
			message.setFrom(mailProperties.getFrom());
			message.setTo(event.email());
			message.setSubject(subject);
			message.setText(buildUserCreatedEmailBody(event));

			mailSender.send(message);
			log.info("Email sent for user {}", event.email());
		} catch (Exception ex) {
			log.error("Failed to register email for user {}", event.email(), ex);
			throw ex;
		}
	}

	@Override
	public void sendUserDeletedEmail(UserDeletedEvent event) {
		String subject = "User Confirmation: " + event.email();

		try {
			SimpleMailMessage message = new SimpleMailMessage();
			message.setFrom(mailProperties.getFrom());
			message.setTo(event.email());
			message.setSubject(subject);
			message.setText(buildUserDeletedEmailBody(event));

			mailSender.send(message);
			log.info("Email sent for user {}", event.email());
		} catch (Exception ex) {
			log.error("Failed to register email for user {}", event.email(), ex);
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
			""".formatted(event.orderId(), event.items(), event.totalPrice());
	}

	private String buildPaymentCompletedEmailBody(PaymentCompletedEvent event) {
		return """
			Your payment has been received!
			
			Order ID: %s
			Amount: %s
			
			Thank you for your purchase.
			""".formatted(event.orderId(), event.amount());
	}

	private String buildPaymentFailedEmailBody(PaymentFailedEvent event) {
		return """
			Your payment has failed!
			
			Order ID: %s
			Amount: %s
			
			Please try again!.
			""".formatted(event.orderId(), event.amount());
	}

	private String buildReservationCompletedEmailBody(InventoryReservationCompletedEvent event) {
		return """
			Your reservation has been completed!
			
			Order ID: %s
			
			Thank you for your purchase.
			""".formatted(event.orderId());
	}

	private String buildReservationFailedEmailBody(InventoryReservationFailedEvent event) {
		return """
			Your reservation has failed!
			
			Order ID: %s
			Reason: %s
			
			Please try again!.
			""".formatted(event.orderId(), event.reason());
	}

	private String buildUserCreatedEmailBody(UserCreatedEvent event) {
		return """
			User has been created!
			
			Mail: %s
			
			Thank you for your registration.
			""".formatted(event.email());
	}

	private String buildUserDeletedEmailBody(UserDeletedEvent event) {
		return """
			User has been deleted!
			
			Mail: %s
			
			Thank you for your registration.
			""".formatted(event.email());
	}
}