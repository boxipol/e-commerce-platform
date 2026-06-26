package com.pd.ecommerce.integration;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.output.MigrateResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@DisplayName("Flyway Migration Integration Tests")
class FlywayMigrationIntegrationTest {

	static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>(DockerImageName.parse("postgres:16-alpine"))
		.withDatabaseName("payments_db")
		.withUsername("ecommerce_user")
		.withPassword("ecommerce_pass");

	static {
		POSTGRES.start();
	}

	private Flyway flyway() {
		return Flyway.configure()
			.dataSource(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword())
			.locations("classpath:db/migration")
			.cleanDisabled(false)
			.load();
	}

	private Connection jdbcConnection() throws Exception {
		return DriverManager.getConnection(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword());
	}

	@Test
	@DisplayName("migrate - should create payments schema on empty database")
	void migrateCreatesPaymentsSchema() throws Exception {
		Flyway flyway = flyway();
		flyway.clean();

		MigrateResult result = flyway.migrate();
		assertThat(result.migrationsExecuted).isEqualTo(1);

		try (
			Connection connection = jdbcConnection();
			Statement stmt = connection.createStatement();
			ResultSet rs = stmt.executeQuery("""
				SELECT EXISTS (
				  SELECT 1
				  FROM information_schema.tables
				  WHERE table_schema = 'public' AND table_name = 'payments'
				)
				""")
		) {
			assertThat(rs.next()).isTrue();
			assertThat(rs.getBoolean(1)).isTrue();
		}
	}

	@Test
	@DisplayName("migrate - should be idempotent when rerun")
	void migrateIsIdempotent() {
		Flyway flyway = flyway();
		flyway.clean();

		MigrateResult firstRun = flyway.migrate();
		MigrateResult secondRun = flyway.migrate();

		assertThat(firstRun.migrationsExecuted).isEqualTo(1);
		assertThat(secondRun.migrationsExecuted).isEqualTo(0);
	}
}