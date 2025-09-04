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
 * Input file name: stock_pricesDaruCar.txt
 * Input format for BMW 01.2016
 * OEM Number       |  Description       |  Price
 * 01402913924      \t Owner's Manu      \t 88.71
 */

/**
 * Created by Dobrev-DAT on 29.1.2023 г..
 */
@Slf4j
public final class BMW extends VehicleBrandTask {

	private static final Charset FIAT_INPUT_ENCODING = StandardCharsets.UTF_8;
	private static final byte NUMBER_COLUMN = 0;
	private static final byte PRICE_COLUMN = 2;

	public BMW() throws Exception { // todo temporary until VehicleBrandTask process all and not abstract
		super(Brand.BMW);
	}

	public void run() {
		log.info("Starting {} process...", brand);

		try (
			FileInputStream inputStream = new FileInputStream(inputFile.toString());
			FileOutputStream logOutputStream = new FileOutputStream(logFile.toString());
			Writer logWriter = new OutputStreamWriter(logOutputStream, DEFAULT_OUTPUT_ENCODING);
			BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, FIAT_INPUT_ENCODING))
		){
			String inputLine;

			while ((inputLine = reader.readLine()) != null) {
				String[] lineStrings = inputLine.split(TAB);

				if (lineStrings.length >= 4) {
					addPart(lineStrings[NUMBER_COLUMN], lineStrings[PRICE_COLUMN], logWriter);
				}
			}

			writeOutputFile(outputFile);
			compress(outputFile, zipFile);

			log.info(zipFile.getFileName().toString());
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