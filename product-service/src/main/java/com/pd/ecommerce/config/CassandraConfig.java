package com.pd.ecommerce.config;

import com.datastax.oss.driver.api.core.CqlSession;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

@Configuration
@RequiredArgsConstructor
public class CassandraConfig {

	private final CassandraProperties cassandraProperties;


	@Bean
	public CqlSession session() {
		try (CqlSession initSession = buildSession(null)) {
			runMigrations(initSession);
		}

		return buildSession(cassandraProperties.getKeyspaceName());
	}

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
		try (
			InputStream is = getClass().getClassLoader().getResourceAsStream("db/migration/V1__create_products_schema.cql")
		) {
			if (is == null) {
				throw new IllegalStateException("Migration file not found");
			}

			String cql = new String(is.readAllBytes(), StandardCharsets.UTF_8);

			Arrays.stream(cql.split(";"))
				.map(String::trim)
				.filter(s -> !s.isBlank())
				.forEach(session::execute);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}
}