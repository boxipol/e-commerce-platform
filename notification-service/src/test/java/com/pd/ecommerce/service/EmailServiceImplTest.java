package com.pd.ecommerce.service;

import com.pd.ecommerce.config.MailProperties;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@RequiredArgsConstructor
class EmailServiceImplTest {

	private final MailProperties mailProperties;


	@Test
	void sendOrderCreatedEmail() {
//		Properties properties = new Properties();
//		properties.put("mail.smtp.auth", mailProperties.getProperties().get("mail.smtp.auth"));
//		properties.put("mail.smtp.starttls.enable", mailProperties.getProperties().get("mail.smtp.starttls.enable"));
//		Session session = Session.getInstance(properties);
//
//		assertDoesNotThrow(() -> {
//			try (Transport transport = session.getTransport("smtp")) {
//				transport.connect(
//					mailProperties.getHost(),
//					mailProperties.getPort(),
//					mailProperties.getUsername(),
//					mailProperties.getPassword()
//				);
//
//				System.out.println("SMTP connection successful");
//			}
//		});
	}

	@Test
	void sendPaymentCompletedEmail() {
	}

	@Test
	void sendPaymentFailedEmail() {
	}

	@Test
	void sendUserCreatedEmail() {
	}
}