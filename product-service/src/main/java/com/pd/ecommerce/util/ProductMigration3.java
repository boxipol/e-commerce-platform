package com.pd.ecommerce.util;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.BatchStatement;
import com.datastax.oss.driver.api.core.cql.BatchStatementBuilder;
import com.datastax.oss.driver.api.core.cql.DefaultBatchType;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;
import java.net.InetSocketAddress;

public class ProductMigration3 {

	private static final int BATCH_SIZE = 500;


	public static void main(String[] args) {
		try (CqlSession session = CqlSession.builder()
			.addContactPoint(new InetSocketAddress("127.0.0.1", 9042))
			.withLocalDatacenter("datacenter1")
			.withKeyspace("ecommerce")
			.build()
		){
			// Source query
			String selectQuery = "SELECT category, created_at, sku, name, brand, price, stock " + "FROM products_by_id";
			ResultSet rs = session.execute(selectQuery);

			// Prepared insert (important for performance)
			PreparedStatement insertStmt = session.prepare("INSERT INTO products_by_category " + "(category, created_at, sku, name, brand, price, stock) " + "VALUES (?, ?, ?, ?, ?, ?, ?)");
			BatchStatementBuilder batch = BatchStatement.builder(DefaultBatchType.UNLOGGED);
			int count = 0;

			for (Row row : rs) {
				batch.addStatement(insertStmt.bind(row.getString("category"), row.getInstant("created_at"), row.getString("sku"), row.getString("name"), row.getString("brand"), row.getBigDecimal("price"), row.getInt("stock")));
				count++;

				// Flush batch periodically
				if (count % BATCH_SIZE == 0) {
					session.execute(batch.build());
					batch = BatchStatement.builder(DefaultBatchType.UNLOGGED);
				}
			}

			// Flush remaining records
			BatchStatement remainingBatch = batch.build();

			if (remainingBatch.size() > 0) {
				session.execute(remainingBatch);
			}

			System.out.println("Migration completed. Total rows: " + count);
		}
	}
}