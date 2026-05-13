package com.pd.ecommerce.config;

import com.datastax.oss.driver.api.core.CqlSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.net.InetSocketAddress;

@Configuration
public class CassandraConfig {

	@Bean
	public CqlSession session(
		@Value("${spring.cassandra.contact-points:cassandra}") String contactPoint,
		@Value("${spring.cassandra.port:9042}") int port,
		@Value("${spring.cassandra.local-datacenter:dc1}") String dc,
		@Value("${spring.cassandra.keyspace-name:ecommerce}") String keyspace
	){
		return CqlSession.builder()
			.addContactPoint(new InetSocketAddress(contactPoint, port))
			.withLocalDatacenter(dc)
			.withKeyspace(keyspace)
			.build();
	}
}