package com.cardata.partpricemanager.service;

import com.cardata.partpricemanager.brands.VehicleBrandTask;
import com.cardata.partpricemanager.config.FtpConfig;
import com.cardata.partpricemanager.config.FtpProperties;
import com.cardata.partpricemanager.models.Brand;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = {FtpConfig.class})
@TestPropertySource(properties = {
	"ftp.servers[0].brand=RENAULT",
	"ftp.servers[0].host=ftp.test1",
	"ftp.servers[0].port=21",
	"ftp.servers[0].user=userA",
	"ftp.servers[0].password=passA",
	"ftp.servers[1].brand=BMW",
	"ftp.servers[1].host=ftp.test2",
	"ftp.servers[1].port=2221",
	"ftp.servers[1].user=userB",
	"ftp.servers[1].password=passB"
})
class PartsServiceImplTest {

//	@Autowired
	private FtpService ftpService;

	@Autowired
	private FtpProperties ftpProperties;


	@BeforeEach
	void setUp() {
		ftpService = new FtpService();
	}

	@AfterEach
	void tearDown() {
	}

	@Test
	void processBrand() {
	}

	@Test
	void shareFolder() {
		System.out.println(ftpProperties.toString());
	}

	@Test
	void downloadSource() throws IOException {
		Path destination = Path.of(String.format("%s/%s/%s", VehicleBrandTask.WORKING_DIR, Brand.RENAULT, "Renault_CarData.txt"));
		FtpProperties.ServerConfig serverConfig = ftpProperties.getConfig(Brand.RENAULT);
		System.out.println(serverConfig);
		ftpService.downloadFile(serverConfig.getHost(), serverConfig.getPort(), serverConfig.getUser(), serverConfig.getPassword(), "Renault_CarData.txt", destination.toString());
		File file = new File(destination.toString());

		assertTrue(file.exists());
	}
}