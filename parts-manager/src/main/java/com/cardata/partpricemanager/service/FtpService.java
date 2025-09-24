package com.cardata.partpricemanager.service;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.springframework.stereotype.Service;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

@Service
public final class FtpService {

	public void downloadFile(String server, int port, String user, String pass, String remoteFilePath, String localFilePath) throws IOException {
		var ftpClient = new FTPClient();

		try {
			ftpClient.connect(server, port);
			boolean loggedIn = ftpClient.login(user, pass);

			if (!loggedIn) {
				throw new IOException("Could not login to FTP server");
			}

			ftpClient.enterLocalPassiveMode(); // recommended for firewalls/NAT
			ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

			try (OutputStream outputStream = new FileOutputStream(localFilePath)) {
				boolean success = ftpClient.retrieveFile(remoteFilePath, outputStream);

				if (!success) {
					throw new IOException("Could not download file: " + remoteFilePath);
				}
			}

			ftpClient.logout();
		} finally {
			if (ftpClient.isConnected()) {
				ftpClient.disconnect();
			}
		}
	}
}