package com.pd.ecommerce.util;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.DriverTimeoutException;
import com.datastax.oss.driver.api.core.config.DefaultDriverOption;
import com.datastax.oss.driver.api.core.config.DriverConfigLoader;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;
import java.util.stream.Collectors;

public class ProductMigration {

	private static final String[] SEED_FILES = {
		"full_iphone_seed_query_products_by_id.sql",
		"full_iphone_seed_query_products_by_sku.sql",
		"full_iphone_seed_query_products_by_category.sql"
	};

	private static final int REQUEST_TIMEOUT_SECONDS = 20;
	private static final int MAX_RETRIES_ON_TIMEOUT = 3;


	public static void main(String[] args) {
		try (
			CqlSession session = CqlSession.builder()
				.addContactPoint(new InetSocketAddress("127.0.0.1", 9042))
				.withLocalDatacenter("datacenter1")
				.withKeyspace("ecommerce")
				.withConfigLoader(
					DriverConfigLoader.programmaticBuilder()
						.withDuration(DefaultDriverOption.REQUEST_TIMEOUT, Duration.ofSeconds(REQUEST_TIMEOUT_SECONDS))
						.build()
				)
				.build()
		){
			int totalStatements = 0;

			for (String seedFile : SEED_FILES) {
				totalStatements += executeSeedFile(session, seedFile);
			}

			System.out.println("Migration completed. Total statements executed: " + totalStatements);
		}
	}

	private static int executeSeedFile(CqlSession session, String resourcePath) {
		try (InputStream is = ProductMigration.class.getClassLoader().getResourceAsStream(resourcePath)) {
			if (is == null) {
				throw new IllegalStateException("Seed file not found: " + resourcePath);
			}

			String raw = new String(is.readAllBytes(), StandardCharsets.UTF_8);
			String withoutComments = Arrays.stream(raw.split("\\R"))
				.filter(line -> !line.stripLeading().startsWith("--"))
				.collect(Collectors.joining("\n"));

			var statements = Arrays.stream(withoutComments.split(";"))
				.map(String::trim)
				.filter(statement -> !statement.isBlank())
				.filter(statement -> !statement.equalsIgnoreCase("USE ecommerce"))
				.toList();

			for (String statement : statements) {
				executeWithRetry(session, statement);
			}

			System.out.println("Executed " + statements.size() + " statements from " + resourcePath);

			return statements.size();
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	private static void executeWithRetry(CqlSession session, String statement) {
		int attempt = 1;

		while (true) {
			try {
				session.execute(statement);
				return;
			} catch (DriverTimeoutException e) {
				if (attempt >= MAX_RETRIES_ON_TIMEOUT) {
					throw e;
				}

				System.out.println("Statement timeout (attempt " + attempt + "), retrying...");
				attempt++;
			}
		}
	}
}