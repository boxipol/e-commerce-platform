package com.pd.ecommerce.repository;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.SimpleStatement;
import com.datastax.oss.driver.api.core.cql.SimpleStatementBuilder;
import com.pd.ecommerce.dto.ProductByCategoryView;
import com.pd.ecommerce.dto.ProductPageResponse;
import com.pd.ecommerce.entity.ProductByCategory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.cassandra.core.convert.CassandraConverter;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import java.nio.ByteBuffer;
import java.util.Base64;
import java.util.List;
import java.util.stream.StreamSupport;

@Repository
@RequiredArgsConstructor
public class ProductByCategoryQueryRepository {

	private final CqlSession session;
	private final CassandraConverter converter;


	public Mono<ProductPageResponse> findByCategory(String category, int pageSize, String pageState) {
		SimpleStatementBuilder builder = SimpleStatement.builder("""
                    SELECT *
                    FROM products_by_category
                    WHERE category = ?
                    """)
			.addPositionalValue(category)
			.setPageSize(pageSize);

		if (pageState != null && !pageState.isBlank()) {
			byte[] bytes = Base64.getUrlDecoder().decode(pageState);
			builder.setPagingState(ByteBuffer.wrap(bytes));
		}

		return Mono.fromCompletionStage(
			session.executeAsync(builder.build()))
			.map(resultSet -> {
				List<ProductByCategoryView> items = StreamSupport.stream(resultSet.currentPage().spliterator(), false)
					.map(row -> converter.read(ProductByCategory.class, row))
					.map(this::toView)
					.toList();

				ByteBuffer state = resultSet.getExecutionInfo()
					.getPagingState();

				String cursor = (state == null)
					? null
					: Base64.getUrlEncoder()
						.encodeToString(state.slice().array());

				return new ProductPageResponse(items, cursor);
			});
	}

	private ProductByCategoryView toView(ProductByCategory product) {
		return ProductByCategoryView.builder()
			.sku(product.getKey().getSku())
			.name(product.getName())
			.brand(product.getBrand())
			.price(product.getPrice())
			.stock(product.getStock())
			.build();
	}
}