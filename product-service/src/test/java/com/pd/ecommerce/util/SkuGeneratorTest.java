package com.pd.ecommerce.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("SkuGenerator Tests")
class SkuGeneratorTest {

	@Test
	@DisplayName("generate - builds SKU from brand, model, storage and color")
	void testGenerateFull() {
		String sku = SkuGenerator.generate("Apple", "iPhone 6", "128GB Silver edition");

		assertThat(sku).isEqualTo("APP-IPHONE6-128GB-S");
	}

	@Test
	@DisplayName("generate - uppercases brand prefix to three letters")
	void testBrandPrefix() {
		String sku = SkuGenerator.generate("samsung", "Galaxy", "64GB black");

		assertThat(sku).startsWith("SAM-");
	}

	@Test
	@DisplayName("generate - strips non-alphanumeric characters from model")
	void testModelSanitised() {
		String sku = SkuGenerator.generate("Sony", "WH-1000 XM5", "256GB white");

		assertThat(sku).isEqualTo("SON-WH1000XM5-256GB-W");
	}

	@Test
	@DisplayName("generate - uses NA storage and X color when not present")
	void testFallbacks() {
		String sku = SkuGenerator.generate("Dell", "XPS", "premium laptop");

		assertThat(sku).isEqualTo("DEL-XPS-NA-X");
	}

	@Test
	@DisplayName("generate - maps black color to B")
	void testBlackColor() {
		String sku = SkuGenerator.generate("Acer", "Nitro", "512GB Black");

		assertThat(sku).endsWith("-B");
	}
}
