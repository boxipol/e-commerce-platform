package com.pd.ecommerce.integration;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.config.DefaultDriverOption;
import com.datastax.oss.driver.api.core.config.DriverConfigLoader;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.CassandraContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

@Testcontainers
@DisplayName("Cassandra Migration Integration Tests")
class CassandraMigrationIntegrationTest {

	@Container
	static final CassandraContainer<?> CASSANDRA = new CassandraContainer<>(DockerImageName.parse("cassandra:4.1"));

	static CqlSession session;


	@BeforeAll
	static void setUpAll() {
		session = CqlSession.builder()
			.addContactPoint(new InetSocketAddress(CASSANDRA.getHost(), CASSANDRA.getFirstMappedPort()))
			.withLocalDatacenter(CASSANDRA.getLocalDatacenter())
			.withConfigLoader(DriverConfigLoader.programmaticBuilder()
				.withDuration(DefaultDriverOption.REQUEST_TIMEOUT, Duration.ofSeconds(30))
				.build())
			.build();
	}

	@AfterAll
	static void tearDownAll() {
		if (session != null) session.close();
	}

	@BeforeEach
	void setUp() {
		session.execute("DROP KEYSPACE IF EXISTS ecommerce");
	}

	private void runMigration() throws IOException {
		try (
			InputStream is = getClass().getClassLoader()
				.getResourceAsStream("db/migration/V1__create_products_schema.cql")
		) {
			assertThat(is).as("migration file must exist on classpath").isNotNull();
			String cql = new String(is.readAllBytes(), StandardCharsets.UTF_8);

			Arrays.stream(cql.split(";"))
				.map(String::trim)
				.filter(s -> !s.isBlank())
				.forEach(session::execute);
		}
	}

	@Test
	@DisplayName("migrate - should create ecommerce keyspace")
	void migrationCreatesKeyspace() throws IOException {
		runMigration();

		var row = session.execute(
			"SELECT keyspace_name FROM system_schema.keyspaces WHERE keyspace_name = 'ecommerce'")
			.one();
		assertThat(row).isNotNull();
	}

	@Test
	@DisplayName("migrate - should create all three product tables")
	void migrationCreatesAllTables() throws IOException {
		runMigration();

		for (String table : Set.of("products_by_id", "products_by_sku", "products_by_category")) {
			var row = session.execute(
				"SELECT table_name FROM system_schema.tables " +
				"WHERE keyspace_name = 'ecommerce' AND table_name = ?", table)
				.one();
			assertThat(row).as("expected table '%s' to exist", table).isNotNull();
		}
	}

	@Test
	@DisplayName("migrate - should be idempotent when rerun")
	void migrationIsIdempotent() throws IOException {
		runMigration();
		assertThatCode(this::runMigration).doesNotThrowAnyException();
	}
}