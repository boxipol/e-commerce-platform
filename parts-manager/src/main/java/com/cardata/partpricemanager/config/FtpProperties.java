package com.cardata.partpricemanager.config;

import com.cardata.partpricemanager.models.Brand;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "ftp")
@Getter
@Setter
public final class FtpProperties {

	private List<ServerConfig> servers = new ArrayList<>();


	@Getter
	@Setter
	public static class ServerConfig {

		private Brand brand;
		private String host;
		private int port;
		private String user;
		private String password;


		@Override
		public String toString() {
			return "ServerConfig{" + "brand=" + brand + ", host='" + host + '\'' + ", port=" + port + ", user='" + user + '\'' + ", password='" + password + '\'' + '}';
		}
	}

	public ServerConfig getConfig(Brand brand) {
		return servers.stream()
			.filter(serverConfig -> serverConfig.brand.equals(brand))
			.findFirst()
			.orElseThrow();
	}

	@Override
	public String toString() {
		return "FtpProperties{" + "servers=" + servers + '}';
	}
}