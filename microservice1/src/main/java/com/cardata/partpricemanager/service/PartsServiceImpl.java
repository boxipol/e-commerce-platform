package com.cardata.partpricemanager.service;

import com.cardata.partpricemanager.brands.VehicleBrandTask;
import com.cardata.partpricemanager.models.Brand;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


@Service
public final class PartsServiceImpl implements PartsService {

	private static final int N_THREADS = Runtime.getRuntime().availableProcessors();
	private static final ExecutorService executor = Executors.newFixedThreadPool(N_THREADS);


	@Override
	public StreamingResponseBody processBrand(Brand brand) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException, IOException, InterruptedException, ExecutionException {

		// downloading are for brands with constant sources - BMW, Opel, Renault
		// fetching


		VehicleBrandTask task = brand.getTaskClass()
			.getDeclaredConstructor()
			.newInstance();

		Future<?> future = executor.submit(task);
		future.get();

		return outputStream -> {
			try (
				InputStream inputStream = Files.newInputStream(Path.of(task.getZipFile()));
				BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream)
			){
				byte[] buffer = new byte[8192];
				int bytesRead;

				while ((bytesRead = bufferedInputStream.read(buffer)) != -1) {
					outputStream.write(buffer, 0, bytesRead);
					outputStream.flush();
				}
			}
		};
	}
}