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
 * Input file name: Car_Data.txt
 * Input format for Toyota 02.2016
 * OEM Number     |Description              |Price
 * 0000000007     |TEST P/N PARTS PLNG      |6.00
 */

/**
 * Created by Dobrev-DAT on 15.2.2016 г..
 */
@Slf4j
public final class TOYOTA extends VehicleBrandTask {

	private static final String LEXUS_ZIP = WORKING_DIR + "/LEXUS/DAT.BG_LEXUS_487_retail_exvat_" + CURRENT_DATE_STR + ".csv" + ".gz";
	private static final String SPLITTER = "\\|";

	private static final Charset TOYOTA_INPUT_ENCODING = StandardCharsets.UTF_8;

	private static final int NUMBER_COLUMN = 0;
	private static final int PRICE_COLUMN = 2;


	public TOYOTA() throws Exception {
		super(Brand.TOYOTA);
	}

	public void run() {
		log.info("Starting {} and Lexus process...", brand);

		try (
			FileInputStream inputStream = new FileInputStream(inputFile.toString());
			FileOutputStream logOutputStream = new FileOutputStream(logFile.toString());
			Writer logWriter = new OutputStreamWriter(logOutputStream, DEFAULT_OUTPUT_ENCODING);
			BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, TOYOTA_INPUT_ENCODING));
		){
			String inputLine;

			while ((inputLine = reader.readLine()) != null) {
				String[] lineStrings = inputLine.split(SPLITTER);

				if (lineStrings.length >= 3) {
					addPart(lineStrings[NUMBER_COLUMN], lineStrings[PRICE_COLUMN], logWriter);
				}
			}

			writeOutputFile(outputFile);
			compress(outputFile, zipFile);
			FileUtils.copyFile(new File(zipFile.toString()), new File(LEXUS_ZIP));

			log.info(zipFile.toString());
			log.info(LEXUS_ZIP);
		} catch (FileNotFoundException e) {
			log.error("{} file not found!", brand);
		} catch (IOException e) {
			log.error("IO Exception");
		}

		log.info("Lines written: {}", getMapSize());
		log.info("Error lines: {}", errorLines);
		log.info("Finished {} and Lexus process...", brand);
	}
}