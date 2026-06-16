package com.pd.ecommerce.integration;

import com.datastax.oss.driver.api.core.CqlSession;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.CassandraContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.net.InetSocketAddress;

/**
 * Base class for product-service integration tests.
 *
 * <p>Starts a real Apache Cassandra node via Testcontainers, creates the {@code ecommerce} keyspace
 * and the product tables, then points the application's Cassandra connection at the container. The
 * container is a shared static singleton, started once for the whole suite. Requires a running
 * Docker daemon.
 *
 * <p>Redis and Kafka auto-configuration are excluded so these tests focus purely on the Cassandra
 * persistence layer.
 */
@Testcontainers
public abstract class AbstractCassandraIntegrationTest {

	private static final String KEYSPACE = "ecommerce";
	private static final String DATACENTER = "datacenter1";

	static final CassandraContainer<?> CASSANDRA =
		new CassandraContainer<>(DockerImageName.parse("cassandra:4.1"));

	static {
		CASSANDRA.start();
		initSchema();
	}

	private static void initSchema() {
		try (CqlSession session = CqlSession.builder()
			.addContactPoint(new InetSocketAddress(CASSANDRA.getHost(), CASSANDRA.getFirstMappedPort()))
			.withLocalDatacenter(CASSANDRA.getLocalDatacenter())
			.build()) {

			session.execute("""
				CREATE KEYSPACE IF NOT EXISTS %s
				WITH replication = {'class': 'SimpleStrategy', 'replication_factor': 1}
				""".formatted(KEYSPACE));

			session.execute("""
				CREATE TABLE IF NOT EXISTS %s.products_by_id (
				    product_id  UUID PRIMARY KEY,
				    sku         TEXT,
				    name        TEXT,
				    description TEXT,
				    brand       TEXT,
				    category    TEXT,
				    price       DECIMAL,
				    currency    TEXT,
				    stock       INT,
				    active      BOOLEAN,
				    created_at  TIMESTAMP,
				    updated_at  TIMESTAMP
				)""".formatted(KEYSPACE));

			session.execute("""
				CREATE TABLE IF NOT EXISTS %s.products_by_sku (
				    sku         TEXT PRIMARY KEY,
				    product_id  UUID,
				    name        TEXT,
				    description TEXT,
				    brand       TEXT,
				    category    TEXT,
				    price       DECIMAL,
				    currency    TEXT,
				    stock       INT,
				    active      BOOLEAN,
				    created_at  TIMESTAMP,
				    updated_at  TIMESTAMP
				)""".formatted(KEYSPACE));

			session.execute("""
				CREATE TABLE IF NOT EXISTS %s.products_by_category (
				    category   TEXT,
				    created_at TIMESTAMP,
				    sku        TEXT,
				    name       TEXT,
				    brand      TEXT,
				    price      DECIMAL,
				    stock      INT,
				    PRIMARY KEY ((category), created_at, sku)
				) WITH CLUSTERING ORDER BY (created_at DESC)""".formatted(KEYSPACE));
		}
	}

	@DynamicPropertySource
	static void registerProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.cassandra.contact-points", CASSANDRA::getHost);
		registry.add("spring.cassandra.port", CASSANDRA::getFirstMappedPort);
		registry.add("spring.cassandra.keyspace-name", () -> KEYSPACE);
		registry.add("spring.cassandra.local-datacenter", () -> DATACENTER);
	}
}
