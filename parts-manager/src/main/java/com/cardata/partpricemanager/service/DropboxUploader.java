package com.cardata.partpricemanager.service;

import com.cardata.partpricemanager.brands.VehicleBrandTask;
import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.WriteMode;
import com.dropbox.core.v2.sharing.AccessLevel;
import com.dropbox.core.v2.sharing.AddMember;
import com.dropbox.core.v2.sharing.MemberSelector;
import com.dropbox.core.v2.sharing.ShareFolderLaunch;
import com.dropbox.core.v2.sharing.SharedFolderMetadata;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;

@Service
public class DropboxUploader {

	private final DbxClientV2 client;


	public DropboxUploader(@Value("${dropbox.access.token}") String accessToken) {
		DbxRequestConfig config = DbxRequestConfig.newBuilder("parts-manager").build();
		client = new DbxClientV2(config, accessToken);
	}

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

	public SharedFolderMetadata shareFolderByMail(String mail) throws DbxException {
		String dropBoxFolderPath = String.format("/Parts %s %s", VehicleBrandTask.CURRENT_DATE.getMonth(), VehicleBrandTask.CURRENT_DATE.getYear());
		ShareFolderLaunch launch = client.sharing().shareFolder(dropBoxFolderPath);
		String dropBoxFolderId = launch.getCompleteValue().getSharedFolderId();
		AddMember member = new AddMember(MemberSelector.email(mail), AccessLevel.OWNER);

		client.sharing()
			.addFolderMember(dropBoxFolderId, Collections.singletonList(member));

		ShareFolderLaunch metadata = client.sharing().shareFolder(dropBoxFolderPath);

		return metadata.getCompleteValue();
	}
}