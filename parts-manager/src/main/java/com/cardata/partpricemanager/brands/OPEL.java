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
 * Input file name: qry_SalesPriceOrig_AUnion.txt
 * Input format for Opel 01.2016
 * OEM Number       |  Description       |  Price
 * EK#1 987 949 585 \t ремък ангренажен  \t 54.60000000000000000000
 */

/**
 * Created by Dobrev-DAT on 11.2.2016 г..
 */
@Slf4j
public final class OPEL extends VehicleBrandTask {

	private static final String CHEVY_ZIP_FILE_PATH = WORKING_DIR + "/CHEVROLET/DAT.BG_CHEVROLET_160_retail_exvat_" + CURRENT_DATE_STR + ARCHIVE_EXT;

	private static final Charset OPEL_INPUT_ENCODING = StandardCharsets.UTF_8;

	private static final int COLUMN_SIZE = 3;
	private static final int NUMBER_COLUMN = 0;
	private static final int PRICE_COLUMN = 2;


	public OPEL() throws Exception {
		super(Brand.OPEL);
	}

	public void run() {
		log.info("Starting {} and Chevrolet process...", brand);

		try (
			FileInputStream inputStream = new FileInputStream(inputFile.toString());
			BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, OPEL_INPUT_ENCODING));
			FileOutputStream logOutputStream = new FileOutputStream(logFile.toString());
			Writer logWriter = new OutputStreamWriter(logOutputStream, DEFAULT_OUTPUT_ENCODING)
		){
			String inputLine;

			while ((inputLine = reader.readLine()) != null) {
				String[] lineStrings = inputLine.split(TAB);

				if (lineStrings.length == COLUMN_SIZE) {
					addPart(lineStrings[NUMBER_COLUMN], lineStrings[PRICE_COLUMN], logWriter);
				}
			}

			writeOutputFile(outputFile);
			compress(outputFile, zipFile);
			FileUtils.copyFile(new File(zipFile.toUri()), new File(CHEVY_ZIP_FILE_PATH));

			log.info(zipFile.toString());
			log.info(CHEVY_ZIP_FILE_PATH);
		} catch (FileNotFoundException e) {
			log.error("{} file not found!", brand);
		} catch (IOException e) {
			log.error("IO Exception");
		}

		log.info("Lines written: {}", getMapSize());
		log.info("Error lines: {}", errorLines);
		log.info("Finished {} and Chevrolet process...", brand);
	}
}