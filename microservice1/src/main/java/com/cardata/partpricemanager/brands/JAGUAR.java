package com.cardata.partpricemanager.brands;

import com.cardata.partpricemanager.models.Brand;
import lombok.extern.slf4j.Slf4j;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/*
 * Input format for Jaguar 02.2016
 * CodeOE    |Price
 * 6001541928;22.07000000000000000000
 */

/**
 * Created by Dobrev-DAT on 11.2.2016 г..
 */
@Slf4j
public final class JAGUAR extends VehicleBrandTask {

	private static final Charset JAGUAR_INPUT_ENCODING = StandardCharsets.UTF_8;

	private static final String SPLITTER = ";";

	private static final int NUMBER_COLUMN = 0;
	private static final int PRICE_COLUMN = 1;


	public JAGUAR() throws Exception {
		super(Brand.JAGUAR);
	}

	public void run() {
		log.info("Starting {} process...", brand);

		try (
			FileInputStream inputStream = new FileInputStream(inputFile);
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, JAGUAR_INPUT_ENCODING));
			FileOutputStream logOutputStream = new FileOutputStream(logFile);
			Writer logWriter = new OutputStreamWriter(logOutputStream, DEFAULT_OUTPUT_ENCODING)
		){
			String inputLine;

			while ((inputLine = bufferedReader.readLine()) != null) {
				String[] lineStrings = inputLine.split(SPLITTER);

				if (lineStrings.length == 2) {
					addPart(lineStrings[NUMBER_COLUMN], lineStrings[PRICE_COLUMN], logWriter);
				}
			}

			writeOutputFile(outputFile);
			compress(outputFile, zipFile);

			log.info(zipFile);
		} catch (FileNotFoundException e) {
			log.error("{} file not found!", brand);
		} catch (IOException e) {
			log.error("IO Exception");
		}

		log.info("Lines written: {}", getMapSize());
		log.info("Error lines: {}", errorLines);
		log.info("Finished {} process...", brand);
	}
}