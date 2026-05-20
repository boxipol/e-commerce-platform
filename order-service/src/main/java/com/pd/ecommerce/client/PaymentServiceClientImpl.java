package com.pd.ecommerce.client;

import com.pd.ecommerce.dto.CreatePaymentRequest;
import com.pd.ecommerce.dto.PaymentResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public final class PaymentServiceClientImpl implements PaymentServiceClient {

	private final WebClient webClient;


	public PaymentServiceClientImpl(WebClient.Builder builder, @Value("${services.payment.url}") String productServiceUrl) {
		this.webClient = builder.baseUrl(productServiceUrl).build();
	}

	public Mono<PaymentResponse> createPayment(CreatePaymentRequest request) {
		return webClient.post()
			.uri("/api/v1/payments")
			.contentType(MediaType.APPLICATION_JSON)
			.bodyValue(request)
			.retrieve()
			.bodyToMono(PaymentResponse.class);
	}
}