package com.pd.ecommerce.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "spring.cassandra")
@Getter
@Setter
public class CassandraProperties {

	private String contactPoints;
	private int port;
	private String localDatacenter;
	private String keyspaceName;
	private String username;
	private String password;
}