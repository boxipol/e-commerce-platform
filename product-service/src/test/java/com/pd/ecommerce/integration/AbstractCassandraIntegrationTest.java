package com.pd.ecommerce.integration;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.config.DefaultDriverOption;
import com.datastax.oss.driver.api.core.config.DriverConfigLoader;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.CassandraContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;

@Testcontainers
public abstract class AbstractCassandraIntegrationTest {

	private static final String KEYSPACE = "ecommerce";
	private static final String DATACENTER = "datacenter1";

	static final CassandraContainer<?> CASSANDRA = new CassandraContainer<>(DockerImageName.parse("cassandra:4.1"));

	static {
		CASSANDRA.start();
		initSchema();
	}

	private static void initSchema() {
		try (
			CqlSession session = CqlSession.builder().addContactPoint(new InetSocketAddress(CASSANDRA.getHost(), CASSANDRA.getFirstMappedPort())).withLocalDatacenter(CASSANDRA.getLocalDatacenter()).withConfigLoader(DriverConfigLoader.programmaticBuilder().withDuration(DefaultDriverOption.REQUEST_TIMEOUT, Duration.ofSeconds(30)).build()).build();
			InputStream is = AbstractCassandraIntegrationTest.class.getClassLoader().getResourceAsStream("db/migration/V1__create_products_schema.cql")
		) {
			if (is == null) {
				throw new IllegalStateException("Migration file not found");
			}

			String cql = new String(is.readAllBytes(), StandardCharsets.UTF_8);
			Arrays.stream(cql.split(";")).map(String::trim).filter(s -> !s.isBlank()).forEach(session::execute);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	@DynamicPropertySource
	static void registerProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.cassandra.contact-points", CASSANDRA::getHost);
		registry.add("spring.cassandra.port", CASSANDRA::getFirstMappedPort);
		registry.add("spring.cassandra.keyspace-name", () -> KEYSPACE);
		registry.add("spring.cassandra.local-datacenter", () -> DATACENTER);
		registry.add("spring.cassandra.username", () -> "cassandra");
		registry.add("spring.cassandra.password", () -> "cassandra");
	}
}