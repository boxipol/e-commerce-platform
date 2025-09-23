package com.cardata.partpricemanager.api;

import com.cardata.partpricemanager.models.Brand;
import com.cardata.partpricemanager.service.MailService;
import com.cardata.partpricemanager.service.PartsService;
import com.dropbox.core.DbxException;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.sharing.SharedFolderMetadata;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.io.IOException;


@RestController
@RequestMapping("/gzip")
@RequiredArgsConstructor
public final class PartsController {

	private final PartsService partsService;


	@GetMapping("/{brand}")
	public ResponseEntity<FileMetadata> processBrand(@PathVariable Brand brand) {
		FileMetadata result;

		try {
			result = partsService.processBrand(brand);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		return ResponseEntity.ok(result);
	}

//	@GetMapping("/{brand}")
//	public ResponseEntity<StreamingResponseBody> downloadGzipFile(@PathVariable Brand brand) {
//		StreamingResponseBody result;
//
//		try {
//			result = partsService.processBrand(brand);
//		} catch (Exception e) {
//			throw new RuntimeException(e);
//		}
//
//		return ResponseEntity.ok()
//			.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"data.gz\"")
//			.header(HttpHeaders.CONTENT_ENCODING, "gzip")
//			.contentType(MediaType.APPLICATION_OCTET_STREAM)
//			.body(result);
//	}

	@GetMapping("/all")
	public ResponseEntity<byte[]> downloadAllGzipFiles() throws IOException {

		return null;
	}

	@GetMapping("/shareFolder")
	public ResponseEntity<SharedFolderMetadata> notifySubscribers() throws DbxException {

		return ResponseEntity.ok(partsService.shareFolder());



//		mailService.sendMail("recipient@example.com", "Hello from Spring", "This is a test email!");


	}
}