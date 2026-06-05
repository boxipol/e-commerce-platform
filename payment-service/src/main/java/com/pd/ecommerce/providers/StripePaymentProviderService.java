package com.pd.ecommerce.providers;

import com.pd.ecommerce.dto.CreateProviderPaymentRequest;
import com.pd.ecommerce.dto.ProviderPaymentResponse;
import com.pd.ecommerce.entity.PaymentProvider;
import com.pd.ecommerce.entity.PaymentStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import java.util.Locale;

@Slf4j
@Component
@RequiredArgsConstructor
public final class StripePaymentProviderService implements PaymentProviderService {

	private final WebClient stripeWebClient;


	@Override
	public Mono<ProviderPaymentResponse> createPayment(CreateProviderPaymentRequest request) {
		log.info("Creating Stripe payment for order {} amount {} {}", request.orderId(), request.amount(), request.currency());

		return stripeWebClient.post()
			.uri("/v1/payment_intents")
			.contentType(MediaType.APPLICATION_FORM_URLENCODED)
			.bodyValue(buildRequest(request))
			.retrieve()
			.bodyToMono(ProviderPaymentResponse.class)
			.doOnSuccess(response ->
				log.info("Stripe payment created successfully paymentIntentId={}", response.id())
			)
			.doOnError(ex ->
				log.error("Failed to create Stripe payment for order {}", request.orderId(), ex)
			);
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

		form.add("metadata[paymentId]", request.paymentId().toString());
		form.add("metadata[orderId]", request.orderId().toString());
		form.add("automatic_payment_methods[enabled]", "true");
		form.add("automatic_payment_methods[allow_redirects]", "never");
		form.add("confirm", "true"); // todo testing, on url provided back
		form.add("payment_method", "pm_card_visa");

		return form;
	}
}