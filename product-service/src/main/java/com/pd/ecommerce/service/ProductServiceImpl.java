package com.pd.ecommerce.service;

import com.pd.ecommerce.dto.ProductCreateRequest;
import com.pd.ecommerce.dto.ProductPageResponse;
import com.pd.ecommerce.dto.ProductResponse;
import com.pd.ecommerce.dto.ProductUpdateRequest;
import com.pd.ecommerce.entity.Product;
import com.pd.ecommerce.entity.ProductByCategory;
import com.pd.ecommerce.entity.ProductByCategoryKey;
import com.pd.ecommerce.mapper.ProductMapper;
import com.pd.ecommerce.repository.ProductByCategoryQueryRepository;
import com.pd.ecommerce.repository.ProductByCategoryRepository;
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
	private final ProductByCategoryRepository productByCategoryRepository;
	private final ProductByCategoryQueryRepository productByCategoryQueryRepository;
	private final ProductMapper productMapper;
	private final ProductCacheService cacheService;


	@Override
	public Mono<ProductResponse> getById(UUID id) {
		return getProductCached(id);
	}

	@Override
	public Flux<ProductResponse> getProducts(List<UUID> ids) {
		return Flux.fromIterable(ids)
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

	public Mono<ProductResponse> create(ProductCreateRequest request) {
		Product product = productMapper.toEntity(request);

		return productRepository.save(product)
			.flatMap(saved ->
				productByCategoryRepository.save(
					productMapper.toProductByCategoryView(saved)
				).thenReturn(saved)
			)
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
							cacheService.evictProduct(getProductKey(id))
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
					.productId(product.getProductId())
					.build();

				return productRepository.deleteById(id)
					.then(productByCategoryRepository.deleteById(key))
					.then(
						cacheService.evictProduct(getProductKey(id))
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

	private Mono<ProductResponse> getProductCached(UUID id) {
		String key = getProductKey(id);

		return cacheService.getProduct(key)
			.doOnNext(response -> log.info("CACHE HIT product id={}", id))
			.switchIfEmpty(
				Mono.defer(() -> {
					log.info("CACHE MISS product id={}", id);

					return productRepository.findById(id)
						.map(productMapper::toResponse)
						.flatMap(dto ->
							cacheService.putProduct(key, dto)
								.thenReturn(dto)
						);
				})
			);
	}

	private String getProductKey(UUID id) {
		return  "product:" + id;
	}
}