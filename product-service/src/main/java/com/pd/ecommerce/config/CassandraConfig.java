package com.pd.ecommerce.config;

import com.datastax.oss.driver.api.core.CqlSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class CassandraConfig {
	
	private static final String MIGRATION_FILE = "db/migration/V1__create_products_schema.cql";

	private final CassandraProperties cassandraProperties;


	@Bean
	public CqlSession session() {
		try (CqlSession initSession = buildSession(null)) {
			runMigrations(initSession);
		}
		return buildSession(cassandraProperties.getKeyspaceName());
	}

//	==================== PRIVATE ====================

	private CqlSession buildSession(String targetKeyspace) {
		var builder = CqlSession.builder()
			.addContactPoint(new InetSocketAddress(
				cassandraProperties.getContactPoints(),
				cassandraProperties.getPort()))
			.withLocalDatacenter(cassandraProperties.getLocalDatacenter())
			.withAuthCredentials(cassandraProperties.getUsername(), cassandraProperties.getPassword());

		return targetKeyspace != null
			? builder.withKeyspace(targetKeyspace).build()
			: builder.build();
	}

	private void runMigrations(CqlSession session) {
		String keyspace = cassandraProperties.getKeyspaceName();
		String version = parseVersion(MIGRATION_FILE);
		String description = parseDescription(MIGRATION_FILE);

		log.info("Database: {}:{}/{} (Cassandra)", cassandraProperties.getContactPoints(), cassandraProperties.getPort(), keyspace);

		long validateStart = System.currentTimeMillis();

		try (InputStream is = getClass().getClassLoader().getResourceAsStream(MIGRATION_FILE)) {
			if (is == null) {
				throw new IllegalStateException("Migration file not found: " + MIGRATION_FILE);
			}

			String cql = new String(is.readAllBytes(), StandardCharsets.UTF_8);

			List<String> statements = Arrays.stream(cql.split(";"))
				.map(String::trim)
				.filter(s -> !s.isBlank())
				.toList();

			log.info("Successfully validated 1 migration (execution time {})",
				formatDuration(System.currentTimeMillis() - validateStart));

			log.info("Current version of schema \"{}\": << Empty Schema >>", keyspace);
			log.info("Migrating schema \"{}\" to version \"{} - {}\"", keyspace, version, description);

			long migrateStart = System.currentTimeMillis();
			statements.forEach(session::execute);

			log.info("Successfully applied 1 migration to schema \"{}\", now at version v{} (execution time {})",
				keyspace, version, formatDuration(System.currentTimeMillis() - migrateStart));

		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	private static String parseVersion(String filename) {
		String name = filename.substring(filename.lastIndexOf('/') + 1);
		return name.substring(1, name.indexOf("__"));
	}

	private static String parseDescription(String filename) {
		String name = filename.substring(filename.lastIndexOf('/') + 1);
		String raw = name.substring(name.indexOf("__") + 2, name.lastIndexOf('.'));

		return raw.replace('_', ' ');
	}

	private static String formatDuration(long millis) {
		return String.format("%02d:%02d.%03ds", millis / 60000, (millis % 60000) / 1000, millis % 1000);
	}
}