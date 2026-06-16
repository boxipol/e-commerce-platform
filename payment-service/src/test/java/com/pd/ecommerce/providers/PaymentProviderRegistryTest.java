package com.pd.ecommerce.providers;

import com.pd.ecommerce.entity.PaymentProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentProviderRegistry Tests")
class PaymentProviderRegistryTest {

	@Mock
	private PaymentProviderService stripeService;

	@Mock
	private PaymentProviderService paypalService;


	@Test
	@DisplayName("get - should return the service matching the requested provider")
	void testGetResolvesProvider() {
		lenient().when(stripeService.provider()).thenReturn(PaymentProvider.STRIPE);
		lenient().when(paypalService.provider()).thenReturn(PaymentProvider.PAYPAL);

		PaymentProviderRegistry registry = new PaymentProviderRegistry(List.of(stripeService, paypalService));

		assertThat(registry.get(PaymentProvider.STRIPE)).isSameAs(stripeService);
		assertThat(registry.get(PaymentProvider.PAYPAL)).isSameAs(paypalService);
	}

	@Test
	@DisplayName("get - should return null for a provider that is not registered")
	void testGetUnknownProvider() {
		when(stripeService.provider()).thenReturn(PaymentProvider.STRIPE);

		PaymentProviderRegistry registry = new PaymentProviderRegistry(List.of(stripeService));
		assertThat(registry.get(PaymentProvider.PAYPAL)).isNull();
	}
}