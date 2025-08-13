package com.cardata.partpricemanager.brands;

import com.cardata.partpricemanager.models.Brand;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

/*
 * Input format for Prices Porsche 072025.xlsx
 * Катал.номер |Наименование      |Цена без ДДС   |Цена с ДДС
 * WHT006457   |   SCREW, HEX     |3,74
 */

/**
 * Created by Dobrev-DAT on 15.2.2016 г..
 */
@Slf4j
public final class PORSCHE extends VehicleBrandTask {

	private static final int NUMBER_COLUMN = 0;
	private static final int PRICE_COLUMN = 2;


	public PORSCHE() throws Exception {
		super(Brand.PORSCHE);
	}

	public void run() {
		log.info("Starting {} process...", brand);

		try (
			FileInputStream fileInputStream = new FileInputStream(inputFile);
			FileOutputStream logOutputStream = new FileOutputStream(logFile);
			Writer logWriter = new OutputStreamWriter(logOutputStream, DEFAULT_OUTPUT_ENCODING);
			XSSFWorkbook xssfWorkbook = new XSSFWorkbook(fileInputStream)
		){
			XSSFSheet xssfSheet = xssfWorkbook.getSheetAt(0);

			for (int rowPointer = 1; rowPointer < xssfSheet.getLastRowNum(); rowPointer++) {
				XSSFRow xssfRow = xssfSheet.getRow(rowPointer);

				if (xssfRow.getCell(NUMBER_COLUMN) == null || xssfRow.getCell(PRICE_COLUMN) == null) {
					System.err.println(xssfRow.getRowNum());
					errorLines++;
					continue;
				}

				addPart(xssfRow.getCell(NUMBER_COLUMN).toString(), xssfRow.getCell(PRICE_COLUMN).toString(), logWriter);
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