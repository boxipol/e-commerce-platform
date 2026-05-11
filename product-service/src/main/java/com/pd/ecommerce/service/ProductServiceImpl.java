package com.pd.ecommerce.service;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.AsyncResultSet;
import com.datastax.oss.driver.api.core.cql.SimpleStatement;
import com.pd.ecommerce.dto.PageResponse;
import com.pd.ecommerce.dto.ProductByCategoryView;
import com.pd.ecommerce.dto.ProductCreateRequest;
import com.pd.ecommerce.dto.ProductMapper;
import com.pd.ecommerce.dto.ProductResponse;
import com.pd.ecommerce.dto.ProductRowMapper;
import com.pd.ecommerce.dto.ProductUpdateRequest;
import com.pd.ecommerce.entity.Product;
import com.pd.ecommerce.repository.ProductByCategoryRepository;
import com.pd.ecommerce.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.stream.StreamSupport;

@Slf4j
@Service
@RequiredArgsConstructor
final class ProductServiceImpl implements ProductService {

	private final ProductRepository productRepository;
	private final ProductByCategoryRepository productByCategoryRepository;
	private final ProductMapper mapper;
	private final ProductRowMapper productRowMapper;
	private final CqlSession session;


	public Mono<ProductResponse> getById(UUID id) {
		return productRepository.findById(id)
			.map(mapper::toResponse);
	}

	public Mono<PageResponse<ProductResponse>> getAll(int limit, String cursor) {
		SimpleStatement statement = SimpleStatement.builder("SELECT * FROM ecommerce.products_by_id").setPageSize(limit).build();

		if (cursor != null && !cursor.isBlank()) {
			statement = statement.setPagingState(ByteBuffer.wrap(Base64.getDecoder().decode(cursor)));
		}

		return Mono.fromCompletionStage(session.executeAsync(statement)).map(this::mapResultSet);
	}

	public Mono<ProductResponse> create(ProductCreateRequest request) {
		return productRepository.save(mapper.toEntity(request))
			.map(mapper::toResponse);
	}

	public Mono<ProductResponse> update(UUID id, ProductUpdateRequest request) {
		return productRepository.findById(id)
			.switchIfEmpty(Mono.error(new RuntimeException("Product not found")))
			.map(existing -> applyUpdate(existing, request))
			.flatMap(productRepository::save)
			.map(mapper::toResponse);
	}

	public Mono<Void> delete(UUID id) {
		return productRepository.deleteById(id);
	}

	public Flux<ProductByCategoryView> getByCategory(String category) {
		return productByCategoryRepository.findByKeyCategory(category)
			.map(mapper::toCategoryView);
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

	private PageResponse<ProductResponse> mapResultSet(AsyncResultSet rs) {
		List<ProductResponse> items = StreamSupport.stream(rs.currentPage().spliterator(), false)
			.map(productRowMapper::toResponse)
			.toList();

		ByteBuffer next = rs.getExecutionInfo().getPagingState();

		return new PageResponse<>(items, next != null ? Base64.getEncoder().encodeToString(next.array()) : null, next != null);
	}

	// Dual-table write orchestration
}