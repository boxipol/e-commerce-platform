package com.cardata.partpricemanager.api;

import com.cardata.partpricemanager.models.Brand;
import com.cardata.partpricemanager.service.PartsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import java.io.IOException;


@RestController
@RequestMapping("/gzip")
@RequiredArgsConstructor
public final class PartsController {

	private final PartsService partsService;


	@GetMapping("/{brand}")
	public ResponseEntity<StreamingResponseBody> downloadGzipFile(@PathVariable Brand brand) {
		StreamingResponseBody result;

		try {
			result = partsService.processBrand(brand);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		return ResponseEntity.ok()
			.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"data.gz\"")
			.header(HttpHeaders.CONTENT_ENCODING, "gzip")
			.contentType(MediaType.APPLICATION_OCTET_STREAM)
			.body(result);
	}

	@GetMapping("/all")
	public ResponseEntity<byte[]> downloadAllGzipFiles(@RequestParam String filePath) throws IOException {

		return null;
	}
}