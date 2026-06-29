package com.pd.ecommerce.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class InventoryMigration {

	private static final String SEED_FILE = "full_iphone_inventory_seed.sql";
	private static final String DB_HOST = System.getenv().getOrDefault("INVENTORY_DB_HOST", "localhost");
	private static final String DB_PORT = System.getenv().getOrDefault("INVENTORY_DB_PORT", "5435");
	private static final String DB_NAME = System.getenv().getOrDefault("INVENTORY_DB_NAME", "inventory_db");
	private static final String JDBC_URL = System.getenv("INVENTORY_DB_JDBC_URL");
	private static final String DB_USER = System.getenv().getOrDefault("POSTGRES_USER", "ecommerce_user");
	private static final String DB_PASSWORD = System.getenv().getOrDefault("POSTGRES_PASSWORD", "ecommerce_pass");


	public static void main(String[] args) {
		List<String> jdbcUrls = candidateJdbcUrls();
		List<String> failures = new ArrayList<>();

		for (String jdbcUrl : jdbcUrls) {
			try (Connection connection = DriverManager.getConnection(jdbcUrl, DB_USER, DB_PASSWORD)) {
				connection.setAutoCommit(false);
				int executed = executeSeedFile(connection, SEED_FILE);
				connection.commit();
				System.out.println("Connected using: " + jdbcUrl);
				System.out.println("Migration completed. Total statements executed: " + executed);
				return;
			} catch (SQLException e) {
				failures.add(jdbcUrl + " -> " + e.getMessage());
			}
		}

		throw new RuntimeException(
			"Failed to run inventory migration. Tried URLs: " + String.join(", ", jdbcUrls)
				+ ". You can override with INVENTORY_DB_JDBC_URL. Errors: " + String.join(" | ", failures)
		);
	}

	private static List<String> candidateJdbcUrls() {
		if (JDBC_URL != null && !JDBC_URL.isBlank()) {
			return List.of(JDBC_URL.trim());
		}

		List<String> urls = new ArrayList<>();
		urls.add("jdbc:postgresql://" + DB_HOST + ":" + DB_PORT + "/" + DB_NAME);
		urls.add("jdbc:postgresql://localhost:5432/" + DB_NAME);
		urls.add("jdbc:postgresql://localhost:5435/" + DB_NAME);
		urls.add("jdbc:postgresql://inventory-db:5432/" + DB_NAME);

		return urls.stream().distinct().toList();
	}

	private static int executeSeedFile(Connection connection, String resourcePath) {
		try (InputStream is = InventoryMigration.class.getClassLoader().getResourceAsStream(resourcePath)) {
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
				.toList();

			try (Statement statement = connection.createStatement()) {
				for (String sql : statements) {
					statement.execute(sql);
				}
			}

			System.out.println("Executed " + statements.size() + " statements from " + resourcePath);
			return statements.size();
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		} catch (SQLException e) {
			throw new RuntimeException("Failed executing seed SQL from " + resourcePath, e);
		}
	}
}