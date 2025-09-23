package com.cardata.partpricemanager.service;

import com.cardata.partpricemanager.brands.VehicleBrandTask;
import com.cardata.partpricemanager.config.FtpProperties;
import com.cardata.partpricemanager.models.Brand;
import com.dropbox.core.DbxException;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.sharing.SharedFolderMetadata;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Slf4j
@Service
public final class PartsServiceImpl implements PartsService {

	@Autowired
	private FtpProperties ftpProperties;

	@Autowired
	private FtpService ftpService;

	@Autowired
	private DropboxUploader uploader;

	private static final int N_THREADS = Runtime.getRuntime().availableProcessors();
	private static final ExecutorService executor = Executors.newFixedThreadPool(N_THREADS);


	@Override
	public FileMetadata processBrand(Brand brand) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException, IOException, InterruptedException, ExecutionException {
		// if the brand can auto-download
		// call to another ms to fetch the entire file

		// todo temp
		if (brand.equals(Brand.BMW) || brand.equals(Brand.OPEL)) {
			String fileUrl = switch (brand) {
				case BMW -> "http://www.bdsoftltd.com/Daru/stock_pricesDaruCar.txt";
				case OPEL -> "http://opelcar.info/bulvaria/prlist.txt";
				default -> "";
			};

			Path destination = Path.of(String.format("%s/%s/%s", VehicleBrandTask.WORKING_DIR, brand, fileUrl.substring(fileUrl.lastIndexOf('/') + 1)));

			try (InputStream in = new URL(fileUrl).openStream()) {
				Files.copy(in, destination);
			} catch (IOException e) {
				log.error("Unable to download source file for {}: {}", brand, e.getMessage());
				throw e;
			}
		}

		if (brand.equals(Brand.RENAULT)) {
			Path destination = Path.of(String.format("%s/%s/%s", VehicleBrandTask.WORKING_DIR, brand, "Renault_CarData.txt"));
			FtpProperties.ServerConfig serverConfig = ftpProperties.getConfig(Brand.RENAULT);
			ftpService.downloadFile(serverConfig.getHost(), serverConfig.getPort(), serverConfig.getUser(), serverConfig.getPassword(), "Renault_CarData.txt", destination.toString());
		}

		VehicleBrandTask task = brand.getTaskClass()
			.getDeclaredConstructor()
			.newInstance();

		Future<?> future = executor.submit(task);
		future.get();

		String dropBoxDestination = String.format("/Parts %s %s/%s", VehicleBrandTask.CURRENT_DATE.getMonth(), VehicleBrandTask.CURRENT_DATE.getYear(), task.getZipFile().getFileName());

		return uploader.uploadFile(task.getZipFile(), dropBoxDestination);
	}

	@Override
	public SharedFolderMetadata shareFolder() throws DbxException {
		String mail = "grozdanov@car-data.bg";
		SharedFolderMetadata metadata = uploader.shareFolderByMail(mail);

		log.info("Invitation sent to {}", mail);

		return metadata;
	}
}