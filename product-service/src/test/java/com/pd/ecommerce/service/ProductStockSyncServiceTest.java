package com.pd.ecommerce.service;

import com.pd.ecommerce.entity.Product;
import com.pd.ecommerce.entity.ProductByCategory;
import com.pd.ecommerce.entity.ProductByCategoryKey;
import com.pd.ecommerce.entity.ProductBySku;
import com.pd.ecommerce.event.OrderEventItem;
import com.pd.ecommerce.repository.ProductByCategoryRepository;
import com.pd.ecommerce.repository.ProductBySkuRepository;
import com.pd.ecommerce.repository.ProductRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductStockSyncService")
class ProductStockSyncServiceTest {

	@Mock
	private ProductBySkuRepository productBySkuRepository;

	@Mock
	private ProductRepository productRepository;

	@Mock
	private ProductByCategoryRepository productByCategoryRepository;

	@Mock
	private ProductCacheService cacheService;

	@InjectMocks
	private ProductStockSyncService service;

	@Test
	@DisplayName("decreaseStockForReservation - should update all projections and evict cache")
	void decreaseStockForReservation_success() {
		UUID orderId = UUID.randomUUID();
		UUID productId = UUID.randomUUID();
		OrderEventItem item = new OrderEventItem(productId, "SKU-1", 3);

		ProductBySku bySku = new ProductBySku();
		bySku.setSku("SKU-1");
		bySku.setProductId(productId);
		bySku.setCategory("Phones");
		bySku.setCreatedAt(Instant.now());
		bySku.setName("iPhone");
		bySku.setBrand("Apple");
		bySku.setPrice(new BigDecimal("999.99"));
		bySku.setStock(10);

		Product byId = new Product();
		byId.setProductId(productId);
		byId.setSku("SKU-1");
		byId.setStock(10);

		when(productBySkuRepository.findById("SKU-1")).thenReturn(Mono.just(bySku));
		when(productBySkuRepository.save(any(ProductBySku.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));
		when(productRepository.findById(productId)).thenReturn(Mono.just(byId));
		when(productRepository.save(any(Product.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));
		when(productByCategoryRepository.save(any())).thenReturn(Mono.just(
			ProductByCategory.builder()
				.key(ProductByCategoryKey.builder().category("Phones").createdAt(bySku.getCreatedAt()).sku("SKU-1").build())
				.name("iPhone")
				.brand("Apple")
				.price(new BigDecimal("999.99"))
				.stock(7)
				.build()
		));
		when(cacheService.evictProduct("product:SKU-1")).thenReturn(Mono.empty());

		StepVerifier.create(service.decreaseStockForReservation(orderId, List.of(item)))
			.verifyComplete();

		verify(productBySkuRepository).save(any(ProductBySku.class));
		verify(productRepository).save(any(Product.class));
		verify(productByCategoryRepository).save(any());
		verify(cacheService).evictProduct("product:SKU-1");
	}

	@Test
	@DisplayName("decreaseStockForReservation - should fail when stock is insufficient")
	void decreaseStockForReservation_insufficientStock() {
		UUID orderId = UUID.randomUUID();
		UUID productId = UUID.randomUUID();
		OrderEventItem item = new OrderEventItem(productId, "SKU-1", 3);

		ProductBySku bySku = new ProductBySku();
		bySku.setSku("SKU-1");
		bySku.setStock(1);

		when(productBySkuRepository.findById("SKU-1")).thenReturn(Mono.just(bySku));

		StepVerifier.create(service.decreaseStockForReservation(orderId, List.of(item)))
			.expectError(IllegalStateException.class)
			.verify();

		verify(productBySkuRepository, never()).save(any());
		verify(productRepository, never()).save(any());
	}
}
