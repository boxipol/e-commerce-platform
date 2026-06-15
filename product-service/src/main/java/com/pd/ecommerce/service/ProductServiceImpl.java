package com.pd.ecommerce.service;

import com.pd.ecommerce.dto.ProductCreateRequest;
import com.pd.ecommerce.dto.ProductPageResponse;
import com.pd.ecommerce.dto.ProductResponse;
import com.pd.ecommerce.dto.ProductUpdateRequest;
import com.pd.ecommerce.entity.Product;
import com.pd.ecommerce.entity.ProductByCategory;
import com.pd.ecommerce.entity.ProductByCategoryKey;
import com.pd.ecommerce.entity.ProductBySku;
import com.pd.ecommerce.mapper.ProductMapper;
import com.pd.ecommerce.repository.ProductByCategoryQueryRepository;
import com.pd.ecommerce.repository.ProductByCategoryRepository;
import com.pd.ecommerce.repository.ProductBySkuRepository;
import com.pd.ecommerce.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
final class ProductServiceImpl implements ProductService {

	private final ProductRepository productRepository;
	private final ProductBySkuRepository productBySkuRepository;
	private final ProductByCategoryRepository productByCategoryRepository;
	private final ProductByCategoryQueryRepository productByCategoryQueryRepository;
	private final ProductMapper productMapper;
	private final ProductCacheService cacheService;


	@Override
	public Mono<ProductResponse> getBySku(String sku) {
		return getProductCached(sku);
	}

	@Override
	public Flux<ProductResponse> getProducts(List<String> skus) {
		return Flux.fromIterable(skus)
			.flatMap(this::getProductCached, 32);
	}

	@Override
	public Mono<ProductPageResponse> getByCategory(String category, int pageSize, String pageState) {
		String cacheKey = cacheService.key(category, pageSize, pageState);

		return cacheService.getProducts(cacheKey)
			.doOnNext(response -> log.info("CACHE HIT category={}", category))
			.switchIfEmpty(
				Mono.defer(() -> {
					log.info("CACHE MISS category={}", category);

					return productByCategoryQueryRepository.findByCategory(category, pageSize, pageState)
						.flatMap(response ->
							cacheService.putProducts(cacheKey, response)
								.thenReturn(response)
						);
				})
			);
	}

	// todo use event driven updates
	public Mono<ProductResponse> create(ProductCreateRequest request) {
		Product product = productMapper.toProduct(request);

		return productRepository.save(product)
			.flatMap(saved -> {
				ProductBySku productBySku = productMapper.toProductBySku(request);
				Mono<ProductBySku> skuSave = productBySkuRepository.save(productBySku);

				Mono<ProductByCategory> categorySave = productByCategoryRepository.save(
					productMapper.toProductByCategoryView(saved)
				);

				return Mono.when(skuSave, categorySave)
					.thenReturn(saved);
			})
			.map(productMapper::toResponse);
	}

	public Mono<ProductResponse> update(UUID id, ProductUpdateRequest request) {
		return productRepository.findById(id)
			.switchIfEmpty(
				Mono.error(new RuntimeException("Product not found")))
			.flatMap(existing -> {
				String oldCategory = existing.getCategory();
				Product updated = applyUpdate(existing, request);
				ProductByCategory oldProjection = productMapper.toProductByCategoryView(existing);
				ProductByCategory newProjection = productMapper.toProductByCategoryView(updated);

				return productRepository.save(updated)
					.flatMap(saved -> {
						Mono<Void> projectionOperation;

						if (!oldCategory.equals(saved.getCategory())) {
							projectionOperation = productByCategoryRepository.delete(oldProjection)
								.then(productByCategoryRepository.save(newProjection))
								.then();
						} else {
							projectionOperation = productByCategoryRepository.save(newProjection)
								.then();
						}

						return projectionOperation.then(
							cacheService.evictProduct(getProductKey(saved.getSku()))
								.onErrorResume(ex -> Mono.empty())
						).thenReturn(saved);
					});
			})
			.map(productMapper::toResponse);
	}

	public Mono<Void> delete(UUID id) {
		return productRepository.findById(id)
			.switchIfEmpty(
				Mono.error(new RuntimeException("Product not found")))
			.flatMap(product -> {
				ProductByCategoryKey key = ProductByCategoryKey.builder()
					.category(product.getCategory())
					.createdAt(product.getCreatedAt())
					.sku(product.getSku())
					.build();

				return productRepository.deleteById(id)
					.then(productByCategoryRepository.deleteById(key))
					.then(
						cacheService.evictProduct(getProductKey(product.getSku()))
							.onErrorResume(ex -> Mono.empty())
					);
			});
	}

//	==================== PRIVATE ====================

	private Product applyUpdate(Product existing, ProductUpdateRequest request) {
		if (request.name() != null) {
			existing.setName(request.name());
		}

		if (request.description() != null) {
			existing.setDescription(request.description());
		}

		if (request.price() != null) {
			existing.setPrice(request.price());
		}

		if (request.stock() != null) {
			existing.setStock(request.stock());
		}

		existing.setUpdatedAt(Instant.now());

		return existing;
	}

	private Mono<ProductResponse> getProductCached(String sku) {
		String key = getProductKey(sku);

		return cacheService.getProduct(key)
			.doOnNext(response -> log.info("CACHE HIT product id={}", sku))
			.switchIfEmpty(
				Mono.defer(() -> {
					log.info("CACHE MISS product id={}", sku);

					return productBySkuRepository.findById(sku)
						.map(productMapper::toResponse)
						.flatMap(dto ->
							cacheService.putProduct(key, dto)
								.thenReturn(dto)
						);
				})
			);
	}

	private String getProductKey(String sku) {
		return  "product:" + sku;
	}
}