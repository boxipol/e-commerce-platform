package com.pd.ecommerce.providers;

import com.pd.ecommerce.dto.CreateProviderPaymentRequest;
import com.pd.ecommerce.dto.ProviderPaymentResponse;
import com.pd.ecommerce.entity.PaymentProvider;
import com.pd.ecommerce.entity.PaymentStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public final class PaypalPaymentProviderService implements PaymentProviderService {

//	private final WebClient paypalWebClient;


	@Override
	public Mono<ProviderPaymentResponse> createPayment(CreateProviderPaymentRequest request) {
		return null;
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
		return PaymentProvider.PAYPAL;
	}
}