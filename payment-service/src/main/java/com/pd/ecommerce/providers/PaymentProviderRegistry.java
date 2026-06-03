package com.pd.ecommerce.providers;

import com.pd.ecommerce.entity.PaymentProvider;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public final class PaymentProviderRegistry {

	private final Map<PaymentProvider, PaymentProviderService> providers;


	public PaymentProviderRegistry(List<PaymentProviderService> services) {
		this.providers = services.stream()
			.collect(Collectors.toMap(PaymentProviderService::provider, Function.identity()));
	}

	public PaymentProviderService get(PaymentProvider provider) {
		return providers.get(provider);
	}
}