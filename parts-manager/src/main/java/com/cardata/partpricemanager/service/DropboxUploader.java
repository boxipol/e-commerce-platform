package com.cardata.partpricemanager.service;

import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.WriteMode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;


@Service
public class DropboxUploader {

	@Value("${dropbox.access.token}")
	private String accessToken;


	public FileMetadata uploadFile(Path localFilePath, String dropboxPath) throws IOException {
		DbxRequestConfig config = DbxRequestConfig.newBuilder("parts-manager").build();
		DbxClientV2 client = new DbxClientV2(config, accessToken);

	public FileMetadata uploadFile(Path localFilePath, String dropboxPath) throws IOException {
		try (var input = new FileInputStream(localFilePath.toString())) {
			return client.files()
				.uploadBuilder(dropboxPath)
				.withMode(WriteMode.OVERWRITE)
				.uploadAndFinish(input);
		} catch (Exception e) {
			throw new IOException("Error uploading to Dropbox", e);
		}
	}
}
