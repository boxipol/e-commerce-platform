package com.cardata.partpricemanager.brands;

import com.cardata.partpricemanager.models.Brand;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import java.io.BufferedReader;
import java.io.File;
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
 * Input format for Renault and Dacia 02.2016
 * CodeOE    |Price                  |Description
 * 6001541928;22.07000000000000000000;NEEDLE BEARING
 */

/**
 * Created by Dobrev-DAT on 8.2.2016 г..
 */
@Slf4j
public final class RENAULT extends VehicleBrandTask {

	private static final String DACIA_ZIP_FILE_PATH = WORKING_DIR + "DACIA/DAT.BG_DACIA_194_retail_exvat_" + CURRENT_DATE_STR + ARCHIVE_EXT;
	private static final String SPLITTER = ";";

	private static final Charset INPUT_ENCODING = StandardCharsets.UTF_8;

	private static final int NUMBER_COLUMN = 0;
	private static final int PRICE_COLUMN = 1;


	public RENAULT() throws Exception {
		super(Brand.RENAULT);
	}

	public void run() {
		log.info("Starting {} and Dacia process...", brand);

		try (
			FileInputStream inputStream = new FileInputStream(inputFile.toString());
			FileOutputStream logOutputStream = new FileOutputStream(logFile.toString());
			Writer logWriter = new OutputStreamWriter(logOutputStream, DEFAULT_OUTPUT_ENCODING);
			BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, INPUT_ENCODING))
		){
			String inputLine;

			while ((inputLine = reader.readLine()) != null) {
				String[] lineStrings = inputLine.split(SPLITTER);

				if (lineStrings.length >= 2) {
					addPart(lineStrings[NUMBER_COLUMN], lineStrings[PRICE_COLUMN], logWriter);
				}
			}

			writeOutputFile(outputFile);
			compress(outputFile, zipFile);
			FileUtils.copyFile(new File(zipFile.toString()), new File(DACIA_ZIP_FILE_PATH));

			log.info(zipFile.toString());
			log.info(DACIA_ZIP_FILE_PATH);
		} catch (FileNotFoundException e) {
			log.error("{} file not found!", brand);
		} catch (IOException e) {
			log.error("IO Exception");
		}

		log.info("Lines written: {}", getMapSize());
		log.info("Error lines: {}", errorLines);
		log.info("Finished {} and Dacia process...", brand);
	}
}