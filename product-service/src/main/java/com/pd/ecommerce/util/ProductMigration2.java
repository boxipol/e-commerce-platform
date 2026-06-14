package com.pd.ecommerce.util;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.BatchStatement;
import com.datastax.oss.driver.api.core.cql.BatchStatementBuilder;
import com.datastax.oss.driver.api.core.cql.DefaultBatchType;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;
import java.net.InetSocketAddress;

public class ProductMigration2 {

	private static final int BATCH_SIZE = 500;


	public static void main(String[] args) {
		try (CqlSession session = CqlSession.builder()
			.addContactPoint(new InetSocketAddress("127.0.0.1", 9042))
			.withLocalDatacenter("datacenter1")
			.withKeyspace("ecommerce")
			.build()
		) {

			// 1. Source query (read from internal table)
			String selectQuery =
				"SELECT sku, product_id, name, description, brand, category, price, currency, stock, active, created_at, updated_at " +
					"FROM products_by_id";

			ResultSet rs = session.execute(selectQuery);

			// 2. Destination insert (SKU-based table)
			PreparedStatement insertStmt = session.prepare(
				"INSERT INTO products_by_sku (" +
					"sku, product_id, name, description, brand, category, price, currency, stock, active, created_at, updated_at" +
					") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
			);

			BatchStatementBuilder batch = BatchStatement.builder(DefaultBatchType.UNLOGGED);

			int count = 0;

			for (Row row : rs) {
				String sku = row.getString("sku");

				if (sku == null) {
					continue; // safety guard
				}

				batch.addStatement(
					insertStmt.bind(
						sku,
						row.getUuid("product_id"),
						row.getString("name"),
						row.getString("description"),
						row.getString("brand"),
						row.getString("category"),
						row.getBigDecimal("price"),
						row.getString("currency"),
						row.getInt("stock"),
						row.getBoolean("active"),
						row.getInstant("created_at"),
						row.getInstant("updated_at")
					)
				);

				count++;

				// flush batch
				if (count % BATCH_SIZE == 0) {
					session.execute(batch.build());
					batch = BatchStatement.builder(DefaultBatchType.UNLOGGED);
				}
			}

			// flush remaining
			BatchStatement remaining = batch.build();

			if (remaining.size() > 0) {
				session.execute(remaining);
			}

			System.out.println("Migration completed. Total rows: " + count);
		}
	}
}