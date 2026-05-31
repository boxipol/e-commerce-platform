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
	private final ProductCacheService productCacheService;


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
		String cacheKey = productCacheService.key(category, pageSize, pageState);

		return productCacheService.getProducts(cacheKey)
			.doOnNext(response -> log.info("CACHE HIT category={}", category))
			.switchIfEmpty(
				Mono.defer(() -> {
					log.info("CACHE MISS category={}", category);

					return productByCategoryQueryRepository.findByCategory(category, pageSize, pageState)
						.flatMap(response ->
							productCacheService.putProducts(cacheKey, response)
								.thenReturn(response)
						);
				})
			);
	}

	public Mono<ProductResponse> create(ProductCreateRequest request) {
		Product product = productMapper.toEntity(request);

		return productByCategoryRepository.save(productMapper.toProductByCategoryView(product))
			.then(productRepository.save(product))
			.map(productMapper::toResponse);
	}

	public Mono<ProductResponse> update(UUID id, ProductUpdateRequest request) {
		return productRepository.findById(id)
			.switchIfEmpty(Mono.error(new RuntimeException("Product not found")))
			.flatMap(existing -> {
				String oldCategory = existing.getCategory();
				Product updated = applyUpdate(existing, request);
				ProductByCategory oldProjection = productMapper.toProductByCategoryView(existing);
				ProductByCategory newProjection = productMapper.toProductByCategoryView(updated);
				Mono<Void> projectionOperation;

				if (!oldCategory.equals(updated.getCategory())) {
					projectionOperation = productByCategoryRepository.delete(oldProjection)
						.then(productByCategoryRepository.save(newProjection))
						.then();
				} else {
					projectionOperation = productByCategoryRepository.save(newProjection)
						.then();
				}

				return productRepository.save(updated)
					.then(projectionOperation)
					.thenReturn(updated);
			})
			.map(productMapper::toResponse);
	}

	public Mono<Void> delete(UUID id) {
		return productRepository.findById(id)
			.switchIfEmpty(Mono.error(new RuntimeException("Product not found")))
			.flatMap(product -> {
				ProductByCategoryKey key =
					ProductByCategoryKey.builder()
						.category(product.getCategory())
						.createdAt(product.getCreatedAt())
						.productId(product.getProductId())
						.build();

				return productRepository.deleteById(id)
					.then(productByCategoryRepository.deleteById(key));
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
		String key = "product:" + id;

		return productCacheService.getProduct(key)
			.doOnNext(v -> log.info("CACHE HIT product id={}", id))
			.switchIfEmpty(
				Mono.defer(() -> {
					log.info("CACHE MISS product id={}", id);

					return productRepository.findById(id)
						.map(productMapper::toResponse)
						.flatMap(dto ->
							productCacheService.putProduct(key, dto)
								.thenReturn(dto)
						);
				})
			);
	}
}