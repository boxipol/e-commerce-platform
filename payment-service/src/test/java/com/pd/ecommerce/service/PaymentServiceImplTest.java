package com.pd.ecommerce.service;

import com.pd.ecommerce.dto.CreateProviderPaymentRequest;
import com.pd.ecommerce.dto.ProviderPaymentResponse;
import com.pd.ecommerce.entity.Payment;
import com.pd.ecommerce.entity.PaymentProvider;
import com.pd.ecommerce.entity.PaymentStatus;
import com.pd.ecommerce.event.OrderCreatedEvent;
import com.pd.ecommerce.event.OrderEventItem;
import com.pd.ecommerce.providers.PaymentProviderRegistry;
import com.pd.ecommerce.providers.PaymentProviderService;
import com.pd.ecommerce.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentServiceImpl")
class PaymentServiceImplTest {

	@Mock
	private PaymentRepository repository;

	@Mock
	private PaymentProviderRegistry paymentProviderRegistry;

	@Mock
	private PaymentProviderService paymentProviderService;

	@InjectMocks
	private PaymentServiceImpl paymentService;
	private static final UUID ORDER_ID = UUID.randomUUID();
	private static final UUID USER_ID = UUID.randomUUID();
	private static final UUID PAYMENT_ID = UUID.randomUUID();
	private OrderCreatedEvent orderEvent;
	private Payment savedPayment;
	private ProviderPaymentResponse providerResponse;


	@BeforeEach
	void setUp() {
		OrderEventItem item = new OrderEventItem(UUID.randomUUID(), "SKU-001", 2);
		orderEvent = OrderCreatedEvent.builder().orderId(ORDER_ID).publicOrderId("ORD-PUBLIC-001").userId(USER_ID).userMail("user@example.com").items(List.of(item)).totalPrice(new BigDecimal("99.99")).build();
		savedPayment = Payment.builder().id(PAYMENT_ID).orderId(ORDER_ID).publicOrderId("ORD-PUBLIC-001").userId(USER_ID).userMail("user@example.com").amount(new BigDecimal("99.99")).items(List.of(item)).currency("EUR").status(PaymentStatus.PENDING).provider(PaymentProvider.STRIPE).createdAt(Instant.now()).updatedAt(Instant.now()).build();
		providerResponse = new ProviderPaymentResponse("stripe-pi-123", "https://stripe.com/pay/123");
	}

	@Nested
	@DisplayName("createPayment")
	class CreatePayment {

		@Test
		@DisplayName("saves payment, calls provider, updates provider data, and returns response")
		void createsPaymentSuccessfully() {
			when(paymentProviderRegistry.get(PaymentProvider.STRIPE)).thenReturn(paymentProviderService);
			when(paymentProviderService.provider()).thenReturn(PaymentProvider.STRIPE);
			when(repository.save(any(Payment.class))).thenReturn(Mono.just(savedPayment));
			when(paymentProviderService.createPayment(any(CreateProviderPaymentRequest.class))).thenReturn(Mono.just(providerResponse));
			when(repository.updateProviderData(eq(PAYMENT_ID), eq("stripe-pi-123"), eq("https://stripe.com/pay/123"), any(Instant.class))).thenReturn(Mono.just(1));
			StepVerifier.create(paymentService.createPayment(orderEvent)).assertNext(response -> {
				assertThat(response.orderId()).isEqualTo(ORDER_ID);
				assertThat(response.publicOrderId()).isEqualTo("ORD-PUBLIC-001");
				assertThat(response.amount()).isEqualByComparingTo("99.99");
				assertThat(response.currency()).isEqualTo("EUR");
				assertThat(response.status()).isEqualTo(PaymentStatus.PENDING);
				assertThat(response.provider()).isEqualTo(PaymentProvider.STRIPE);
				assertThat(response.paymentUrl()).isEqualTo("https://stripe.com/pay/123");
			}).verifyComplete();
			verify(repository).save(any(Payment.class));
			verify(paymentProviderService).createPayment(any(CreateProviderPaymentRequest.class));
			verify(repository).updateProviderData(eq(PAYMENT_ID), eq("stripe-pi-123"), eq("https://stripe.com/pay/123"), any(Instant.class));
		}

		@Test
		@DisplayName("persists payment with PENDING status and correct fields before calling provider")
		void persistsPaymentWithCorrectFields() {
			when(paymentProviderRegistry.get(PaymentProvider.STRIPE)).thenReturn(paymentProviderService);
			when(paymentProviderService.provider()).thenReturn(PaymentProvider.STRIPE);
			when(repository.save(any(Payment.class))).thenReturn(Mono.just(savedPayment));
			when(paymentProviderService.createPayment(any(CreateProviderPaymentRequest.class))).thenReturn(Mono.just(providerResponse));
			when(repository.updateProviderData(any(), any(), any(), any())).thenReturn(Mono.just(1));
			StepVerifier.create(paymentService.createPayment(orderEvent)).expectNextCount(1).verifyComplete();
			ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);
			verify(repository).save(paymentCaptor.capture());
			Payment captured = paymentCaptor.getValue();
			assertThat(captured.getOrderId()).isEqualTo(ORDER_ID);
			assertThat(captured.getUserId()).isEqualTo(USER_ID);
			assertThat(captured.getUserMail()).isEqualTo("user@example.com");
			assertThat(captured.getAmount()).isEqualByComparingTo("99.99");
			assertThat(captured.getCurrency()).isEqualTo("EUR");
			assertThat(captured.getStatus()).isEqualTo(PaymentStatus.PENDING);
			assertThat(captured.getProvider()).isEqualTo(PaymentProvider.STRIPE);
			assertThat(captured.getCreatedAt()).isNotNull();
		}

		@Test
		@DisplayName("passes correct provider request with saved payment ID")
		void passesCorrectProviderRequest() {
			when(paymentProviderRegistry.get(PaymentProvider.STRIPE)).thenReturn(paymentProviderService);
			when(paymentProviderService.provider()).thenReturn(PaymentProvider.STRIPE);
			when(repository.save(any(Payment.class))).thenReturn(Mono.just(savedPayment));
			when(paymentProviderService.createPayment(any(CreateProviderPaymentRequest.class))).thenReturn(Mono.just(providerResponse));
			when(repository.updateProviderData(any(), any(), any(), any())).thenReturn(Mono.just(1));
			StepVerifier.create(paymentService.createPayment(orderEvent)).expectNextCount(1).verifyComplete();
			ArgumentCaptor<CreateProviderPaymentRequest> reqCaptor = ArgumentCaptor.forClass(CreateProviderPaymentRequest.class);
			verify(paymentProviderService).createPayment(reqCaptor.capture());
			CreateProviderPaymentRequest req = reqCaptor.getValue();
			assertThat(req.paymentId()).isEqualTo(PAYMENT_ID);
			assertThat(req.orderId()).isEqualTo(ORDER_ID);
			assertThat(req.amount()).isEqualByComparingTo("99.99");
			assertThat(req.currency()).isEqualTo("EUR");
		}

		@Test
		@DisplayName("propagates error when repository save fails")
		void propagatesErrorOnSaveFailure() {
			when(paymentProviderRegistry.get(PaymentProvider.STRIPE)).thenReturn(paymentProviderService);
			when(paymentProviderService.provider()).thenReturn(PaymentProvider.STRIPE);
			when(repository.save(any(Payment.class))).thenReturn(Mono.error(new RuntimeException("DB unavailable")));
			StepVerifier.create(paymentService.createPayment(orderEvent)).expectErrorMessage("DB unavailable").verify();
		}

		@Test
		@DisplayName("propagates error when provider call fails")
		void propagatesErrorOnProviderFailure() {
			when(paymentProviderRegistry.get(PaymentProvider.STRIPE)).thenReturn(paymentProviderService);
			when(paymentProviderService.provider()).thenReturn(PaymentProvider.STRIPE);
			when(repository.save(any(Payment.class))).thenReturn(Mono.just(savedPayment));
			when(paymentProviderService.createPayment(any(CreateProviderPaymentRequest.class))).thenReturn(Mono.error(new RuntimeException("Stripe API error")));
			StepVerifier.create(paymentService.createPayment(orderEvent)).expectErrorMessage("Stripe API error").verify();
		}
	}
}