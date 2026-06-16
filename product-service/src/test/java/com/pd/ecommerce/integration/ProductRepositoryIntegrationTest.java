package com.pd.ecommerce.integration;

import com.pd.ecommerce.entity.Product;
import com.pd.ecommerce.entity.ProductByCategory;
import com.pd.ecommerce.entity.ProductByCategoryKey;
import com.pd.ecommerce.entity.ProductBySku;
import com.pd.ecommerce.repository.ProductByCategoryRepository;
import com.pd.ecommerce.repository.ProductBySkuRepository;
import com.pd.ecommerce.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.cassandra.DataCassandraTest;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for the product-service Cassandra repositories against a real Cassandra node
 * (Testcontainers). Verifies persistence and reads across the three query-optimized tables:
 * {@code products_by_id}, {@code products_by_sku}, and {@code products_by_category} (including the
 * {@code findByKeyCategory} partition query).
 */
@DataCassandraTest
@DisplayName("Product Cassandra Repository Integration Tests")
class ProductRepositoryIntegrationTest extends AbstractCassandraIntegrationTest {

	@Autowired
	private ProductRepository productRepository;

	@Autowired
	private ProductBySkuRepository productBySkuRepository;

	@Autowired
	private ProductByCategoryRepository productByCategoryRepository;


	@BeforeEach
	void setUp() {
		productRepository.deleteAll().block();
		productBySkuRepository.deleteAll().block();
		productByCategoryRepository.deleteAll().block();
	}

	private Product newProduct(UUID id, String sku, String category) {
		Instant now = Instant.now();
		Product product = new Product();
		product.setProductId(id);
		product.setSku(sku);
		product.setName("iPhone 15");
		product.setDescription("Latest model");
		product.setBrand("Apple");
		product.setCategory(category);
		product.setPrice(new BigDecimal("999.00"));
		product.setCurrency("USD");
		product.setStock(50);
		product.setActive(true);
		product.setCreatedAt(now);
		product.setUpdatedAt(now);
		return product;
	}

	private ProductBySku newProductBySku(UUID id, String sku) {
		Instant now = Instant.now();
		ProductBySku product = new ProductBySku();
		product.setSku(sku);
		product.setProductId(id);
		product.setName("iPhone 15");
		product.setBrand("Apple");
		product.setCategory("phones");
		product.setPrice(new BigDecimal("999.00"));
		product.setCurrency("USD");
		product.setStock(50);
		product.setActive(true);
		product.setCreatedAt(now);
		product.setUpdatedAt(now);
		return product;
	}

	private ProductByCategory newProductByCategory(String category, String sku) {
		return ProductByCategory.builder()
			.key(ProductByCategoryKey.builder()
				.category(category)
				.createdAt(Instant.now())
				.sku(sku)
				.build())
			.name("iPhone 15")
			.brand("Apple")
			.price(new BigDecimal("999.00"))
			.stock(50)
			.build();
	}

	@Test
	@DisplayName("products_by_id - should persist and read back a product by its id")
	void testProductById() {
		UUID id = UUID.randomUUID();

		StepVerifier.create(productRepository.save(newProduct(id, "SKU-1", "phones")))
			.expectNextCount(1)
			.verifyComplete();

		StepVerifier.create(productRepository.findById(id))
			.assertNext(found -> {
				assertThat(found.getSku()).isEqualTo("SKU-1");
				assertThat(found.getBrand()).isEqualTo("Apple");
				assertThat(found.getPrice()).isEqualByComparingTo("999.00");
			})
			.verifyComplete();
	}

	@Test
	@DisplayName("products_by_sku - should persist and read back a product by its sku")
	void testProductBySku() {
		UUID id = UUID.randomUUID();

		productBySkuRepository.save(newProductBySku(id, "SKU-2")).block();

		StepVerifier.create(productBySkuRepository.findById("SKU-2"))
			.assertNext(found -> assertThat(found.getProductId()).isEqualTo(id))
			.verifyComplete();
	}

	@Test
	@DisplayName("products_by_category - findByKeyCategory should return all products in a category")
	void testFindByCategory() {
		productByCategoryRepository.save(newProductByCategory("phones", "SKU-A")).block();
		productByCategoryRepository.save(newProductByCategory("phones", "SKU-B")).block();
		productByCategoryRepository.save(newProductByCategory("laptops", "SKU-C")).block();

		StepVerifier.create(productByCategoryRepository.findByKeyCategory("phones"))
			.expectNextCount(2)
			.verifyComplete();
	}

	@Test
	@DisplayName("products_by_category - findByKeyCategory should be empty for an unknown category")
	void testFindByCategoryEmpty() {
		StepVerifier.create(productByCategoryRepository.findByKeyCategory("unknown"))
			.verifyComplete();
	}
}
