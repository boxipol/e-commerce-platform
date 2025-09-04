package com.cardata.partpricemanager.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


@SpringBootTest
class DropboxUploaderTest {

	@Autowired
	private DropboxUploader uploader;


//	@Test
//	void testUpload() throws Exception {
//		uploader.uploadFile(Path.of("/Users/user/IdeaProjects/spare-parts/Pricelists/2025/Pricelists/OPEL/DAT.BG_OPEL_650_retail_exvat_2025_08_13.csv.gz"), "/Parts 09 2025/DAT.BG_OPEL_650_retail_exvat_2025_08_13.csv.gz");
//	}
}
