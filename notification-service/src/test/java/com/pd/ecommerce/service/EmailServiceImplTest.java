package com.pd.ecommerce.service;

import com.pd.ecommerce.config.MailProperties;
import com.pd.ecommerce.event.OrderCreatedEvent;
import com.pd.ecommerce.event.OrderEventItem;
import com.pd.ecommerce.event.PaymentCompletedEvent;
import com.pd.ecommerce.event.PaymentFailedEvent;
import com.pd.ecommerce.event.UserCreatedEvent;
import com.pd.ecommerce.event.UserDeletedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EmailServiceImpl")
class EmailServiceImplTest {

	@Mock
	private JavaMailSender mailSender;

	@Mock
	private MailProperties mailProperties;

	@InjectMocks
	private EmailServiceImpl emailService;

	private static final String FROM = "noreply@ecommerce.com";
	private static final String TO_MAIL = "customer@example.com";


	@BeforeEach
	void setUp() {
		when(mailProperties.getFrom()).thenReturn(FROM);
	}
	// ── sendOrderCreatedEmail ─────────────────────────────────────────────────

	@Nested
	@DisplayName("sendOrderCreatedEmail")
	class SendOrderCreatedEmail {

		private OrderCreatedEvent event;


		@BeforeEach
		void setUp() {
			event = OrderCreatedEvent.builder().orderId(UUID.randomUUID()).publicOrderId("ORD-ABCD1234").userId(UUID.randomUUID()).userMail(TO_MAIL).items(List.of(new OrderEventItem("SKU-001", 2))).totalPrice(new BigDecimal("199.98")).build();
		}

		@Test
		@DisplayName("sends email to the order's userMail with correct subject and from address")
		void sendsEmailWithCorrectFields() {
			emailService.sendOrderCreatedEmail(event);
			ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
			verify(mailSender).send(captor.capture());
			SimpleMailMessage sent = captor.getValue();
			assertThat(sent.getFrom()).isEqualTo(FROM);
			assertThat(sent.getTo()).containsExactly(TO_MAIL);
			assertThat(sent.getSubject()).isEqualTo("Order Confirmation: ORD-ABCD1234");
		}

		@Test
		@DisplayName("email body contains publicOrderId and totalPrice")
		void bodyContainsOrderDetails() {
			emailService.sendOrderCreatedEmail(event);
			ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
			verify(mailSender).send(captor.capture());
			String body = captor.getValue().getText();
			assertThat(body).contains("ORD-ABCD1234");
			assertThat(body).contains("199.98");
		}

		@Test
		@DisplayName("propagates exception when mailSender throws")
		void propagatesMailSenderException() {
			doThrow(new RuntimeException("SMTP error")).when(mailSender).send(any(SimpleMailMessage.class));
			assertThatThrownBy(() -> emailService.sendOrderCreatedEmail(event)).isInstanceOf(RuntimeException.class).hasMessage("SMTP error");
		}
	}
	// ── sendPaymentCompletedEmail ─────────────────────────────────────────────

	@Nested
	@DisplayName("sendPaymentCompletedEmail")
	class SendPaymentCompletedEmail {

		private PaymentCompletedEvent event;


		@BeforeEach
		void setUp() {
			event = PaymentCompletedEvent.builder().paymentId(UUID.randomUUID()).orderId(UUID.randomUUID()).publicOrderId("ORD-ABCD1234").userMail(TO_MAIL).items(List.of(new OrderEventItem("SKU-001", 2))).amount(new BigDecimal("199.98")).build();
		}

		@Test
		@DisplayName("sends email to the payment's userMail with correct subject and from address")
		void sendsEmailWithCorrectFields() {
			emailService.sendPaymentCompletedEmail(event);
			ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
			verify(mailSender).send(captor.capture());
			SimpleMailMessage sent = captor.getValue();
			assertThat(sent.getFrom()).isEqualTo(FROM);
			assertThat(sent.getTo()).containsExactly(TO_MAIL);
			assertThat(sent.getSubject()).isEqualTo("Payment Confirmation: ORD-ABCD1234");
		}

		@Test
		@DisplayName("email body contains publicOrderId and amount")
		void bodyContainsPaymentDetails() {
			emailService.sendPaymentCompletedEmail(event);
			ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
			verify(mailSender).send(captor.capture());
			String body = captor.getValue().getText();
			assertThat(body).contains("ORD-ABCD1234");
			assertThat(body).contains("199.98");
		}

		@Test
		@DisplayName("propagates exception when mailSender throws")
		void propagatesMailSenderException() {
			doThrow(new RuntimeException("SMTP error")).when(mailSender).send(any(SimpleMailMessage.class));
			assertThatThrownBy(() -> emailService.sendPaymentCompletedEmail(event)).isInstanceOf(RuntimeException.class).hasMessage("SMTP error");
		}
	}
	// ── sendPaymentFailedEmail ────────────────────────────────────────────────

	@Nested
	@DisplayName("sendPaymentFailedEmail")
	class SendPaymentFailedEmail {

		private PaymentFailedEvent event;


		@BeforeEach
		void setUp() {
			event = PaymentFailedEvent.builder().paymentId(UUID.randomUUID()).orderId(UUID.randomUUID()).publicOrderId("ORD-ABCD1234").userMail(TO_MAIL).amount(new BigDecimal("199.98")).build();
		}

		@Test
		@DisplayName("sends email to the payment's userMail with correct subject and from address")
		void sendsEmailWithCorrectFields() {
			emailService.sendPaymentFailedEmail(event);
			ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
			verify(mailSender).send(captor.capture());
			SimpleMailMessage sent = captor.getValue();
			assertThat(sent.getFrom()).isEqualTo(FROM);
			assertThat(sent.getTo()).containsExactly(TO_MAIL);
			assertThat(sent.getSubject()).isEqualTo("Payment Confirmation: ORD-ABCD1234");
		}

		@Test
		@DisplayName("email body contains publicOrderId and amount")
		void bodyContainsPaymentDetails() {
			emailService.sendPaymentFailedEmail(event);
			ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
			verify(mailSender).send(captor.capture());
			String body = captor.getValue().getText();
			assertThat(body).contains("ORD-ABCD1234");
			assertThat(body).contains("199.98");
		}

		@Test
		@DisplayName("email body indicates payment failure")
		void bodyIndicatesFailure() {
			emailService.sendPaymentFailedEmail(event);
			ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
			verify(mailSender).send(captor.capture());
			assertThat(captor.getValue().getText()).contains("failed");
		}

		@Test
		@DisplayName("propagates exception when mailSender throws")
		void propagatesMailSenderException() {
			doThrow(new RuntimeException("SMTP error")).when(mailSender).send(any(SimpleMailMessage.class));
			assertThatThrownBy(() -> emailService.sendPaymentFailedEmail(event)).isInstanceOf(RuntimeException.class).hasMessage("SMTP error");
		}
	}
	// ── sendUserCreatedEmail ──────────────────────────────────────────────────

	@Nested
	@DisplayName("sendUserCreatedEmail")
	class SendUserCreatedEmail {

		private UserCreatedEvent event;


		@BeforeEach
		void setUp() {
			event = UserCreatedEvent.builder().email(TO_MAIL).createdAt(Instant.now()).build();
		}

		@Test
		@DisplayName("sends email to the user's email address with correct subject and from address")
		void sendsEmailWithCorrectFields() {
			emailService.sendUserCreatedEmail(event);
			ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
			verify(mailSender).send(captor.capture());
			SimpleMailMessage sent = captor.getValue();
			assertThat(sent.getFrom()).isEqualTo(FROM);
			assertThat(sent.getTo()).containsExactly(TO_MAIL);
			assertThat(sent.getSubject()).isEqualTo("User Confirmation: " + TO_MAIL);
		}

		@Test
		@DisplayName("email body contains the user's email address")
		void bodyContainsUserEmail() {
			emailService.sendUserCreatedEmail(event);
			ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
			verify(mailSender).send(captor.capture());
			assertThat(captor.getValue().getText()).contains(TO_MAIL);
		}

		@Test
		@DisplayName("propagates exception when mailSender throws")
		void propagatesMailSenderException() {
			doThrow(new RuntimeException("SMTP error")).when(mailSender).send(any(SimpleMailMessage.class));
			assertThatThrownBy(() -> emailService.sendUserCreatedEmail(event)).isInstanceOf(RuntimeException.class).hasMessage("SMTP error");
		}
	}
	// ── sendUserDeletedEmail ──────────────────────────────────────────────────

	@Nested
	@DisplayName("sendUserDeletedEmail")
	class SendUserDeletedEmail {

		private UserDeletedEvent event;


		@BeforeEach
		void setUp() {
			event = UserDeletedEvent.builder().email(TO_MAIL).createdAt(Instant.now()).build();
		}

		@Test
		@DisplayName("sends email to the user's email address with correct subject and from address")
		void sendsEmailWithCorrectFields() {
			emailService.sendUserDeletedEmail(event);
			ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
			verify(mailSender).send(captor.capture());
			SimpleMailMessage sent = captor.getValue();
			assertThat(sent.getFrom()).isEqualTo(FROM);
			assertThat(sent.getTo()).containsExactly(TO_MAIL);
			assertThat(sent.getSubject()).isEqualTo("User Confirmation: " + TO_MAIL);
		}

		@Test
		@DisplayName("email body contains the user's email address")
		void bodyContainsUserEmail() {
			emailService.sendUserDeletedEmail(event);
			ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
			verify(mailSender).send(captor.capture());
			assertThat(captor.getValue().getText()).contains(TO_MAIL);
		}

		@Test
		@DisplayName("email body indicates account deletion")
		void bodyIndicatesDeletion() {
			emailService.sendUserDeletedEmail(event);
			ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
			verify(mailSender).send(captor.capture());
			assertThat(captor.getValue().getText()).contains("deleted");
		}

		@Test
		@DisplayName("propagates exception when mailSender throws")
		void propagatesMailSenderException() {
			doThrow(new RuntimeException("SMTP error")).when(mailSender).send(any(SimpleMailMessage.class));
			assertThatThrownBy(() -> emailService.sendUserDeletedEmail(event)).isInstanceOf(RuntimeException.class).hasMessage("SMTP error");
		}
	}
}