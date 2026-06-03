package com.pd.ecommerce.providers;

import com.pd.ecommerce.dto.CreateProviderPaymentRequest;
import com.pd.ecommerce.dto.ProviderPaymentResponse;
import com.pd.ecommerce.entity.PaymentProvider;
import com.pd.ecommerce.entity.PaymentStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import java.util.Locale;

@Component
@RequiredArgsConstructor
public final class StripePaymentProviderService implements PaymentProviderService {

	private final WebClient stripeWebClient;


	@Override
	public Mono<ProviderPaymentResponse> createPayment(CreateProviderPaymentRequest request) {
		return stripeWebClient.post()
			.uri("/v1/payment_intents")
			.contentType(MediaType.APPLICATION_FORM_URLENCODED)
			.bodyValue(buildRequest(request))
			.retrieve()
			.bodyToMono(ProviderPaymentResponse.class);
	}

	@Override
	public Mono<Void> refund(String paymentId) {
		return null;
	}

	@Override
	public Mono<PaymentStatus> getStatus(String paymentId) {
		return null;
	}

	@Override
	public PaymentProvider provider() {
		return PaymentProvider.STRIPE;
	}

//	==================== PRIVATE ====================

	private MultiValueMap<String, String> buildRequest(CreateProviderPaymentRequest request) {
		LinkedMultiValueMap<String, String> form = new LinkedMultiValueMap<>();

		form.add(
			"amount",
			request.amount()
				.movePointRight(2)
				.toBigInteger()
				.toString()
		);

		form.add(
			"currency",
			request.currency().toLowerCase(Locale.ROOT)
		);

		form.add(
			"metadata[paymentId]",
			request.paymentId().toString()
		);

		form.add(
			"metadata[orderId]",
			request.orderId().toString()
		);

		return form;
	}
}