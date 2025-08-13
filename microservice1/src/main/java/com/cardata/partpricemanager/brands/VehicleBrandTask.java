package com.cardata.partpricemanager.brands;

import com.cardata.partpricemanager.models.Brand;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.javamoney.moneta.Money;
import javax.money.CurrencyUnit;
import javax.money.Monetary;
import javax.money.MonetaryAmount;
import javax.money.format.AmountFormatQueryBuilder;
import javax.money.format.MonetaryAmountFormat;
import javax.money.format.MonetaryFormats;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.zip.GZIPOutputStream;


/**
 * Created by Dobrev-DAT on 2.2.2016 г..
 */
@Slf4j
public abstract class VehicleBrandTask implements Runnable {

	private static final Pattern PART_NO_PATTERN = Pattern.compile("[a-zA-Z0-9/]+");
	private static final Pattern PART_PRICE_PATTERN = Pattern.compile("[0-9., ]+");
	private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy_MM_dd");
	private static final CurrencyUnit BG_CCY_UNIT = Monetary.getCurrency("BGN");
	private static final MonetaryAmountFormat MONETARY_AMOUNT_FORMAT = MonetaryFormats.getAmountFormat(AmountFormatQueryBuilder
		.of(Locale.ENGLISH) // todo test remove
//		.set(CurrencyStyle.NAME) // todo test remove
		.set("pattern", "0.00")
		.build());

	private final Map<String, MonetaryAmount> partPricesMap;

	protected static final Charset DEFAULT_OUTPUT_ENCODING = StandardCharsets.US_ASCII;
	// todo configure location with date
	protected static final String BASE_DIRECTORY = String.format("/Users/user/IdeaProjects/spare-parts/PriceLists/%s/PriceLists/", LocalDate.now().getYear());
	protected static final String ARCHIVE_EXT = ".csv.gz";
	protected static final String CURRENT_DATE = DATE_FORMAT.format(new Date()); // todo remove Date
	protected static final String TAB = "\\t";

	protected final Brand brand;
	protected final String inputFile;

	@Getter
	protected final String outputFile;

	@Getter
	protected final String zipFile;
	protected final String logFile;

	protected int errorLines = 0;


	public VehicleBrandTask(final Brand brand) throws Exception {
		this.brand = brand;

		Path sourceFolderPath = Paths.get(String.format("%s/%s", BASE_DIRECTORY, brand));

		try (Stream<Path> files = Files.list(sourceFolderPath)) {
			List<Path> paths = files
				.filter(path -> !path.toString().contains(".DS_Store"))
				.toList();

			if (paths.size() > 1) {
				throw new Exception("Brand already processed: " + brand);
			}

			inputFile = paths.stream()
				.filter(path -> path
				.toString()
				.toLowerCase()
				.endsWith(brand.getSourceType().getExtension()))
				.findFirst()
				.orElseThrow(() -> new IOException("Source file not found: " + brand))
				.toString();
		}

		outputFile = String.format("%s%s/%s_output.txt", BASE_DIRECTORY, brand, brand);
		zipFile = String.format("%s%s/DAT.BG_%s_%s_retail_exvat_%s%s", BASE_DIRECTORY, brand, brand, brand.getBrandCode(), CURRENT_DATE, ARCHIVE_EXT);
		logFile = String.format("%s%s/%s_log.txt", BASE_DIRECTORY, brand, brand);
		partPricesMap = new HashMap<>(brand.getAverageRecords());
	}

	public abstract void run();

	public void addPart(final String number, final String price, final Writer logWriter) throws IOException {
		String validNumber = this.validatePartNumber(number);
		MonetaryAmount validedPrice = validatePartPrice(price);

		if (validNumber == null || validedPrice == null) {
			logWriter.write(String.format("%s %s\n", number, price));
			errorLines++;

			return;
		}

		if (brand.isRemoveWat()) {
			validedPrice = validedPrice.divide(1.2);
		}

		if (partPricesMap.containsKey(validNumber)) {
			partPricesMap.merge(validNumber, validedPrice, (oldValue, newValue) -> (oldValue.isGreaterThanOrEqualTo(newValue)) ? oldValue : newValue);
		} else {
			partPricesMap.put(validNumber, validedPrice);
		}
	}

	public void writeOutputFile(final String outputFile) throws IOException {
		log.info("Writing file: {}", outputFile);

		try (
			FileOutputStream outputStream = new FileOutputStream(outputFile);
			Writer writer = new OutputStreamWriter(outputStream, DEFAULT_OUTPUT_ENCODING)
		){
			writer.write("\"ETN\";\"NVKPR\"\n");

			partPricesMap.forEach((number, price) -> {
				try {
					writer.write(String.format("\"%s\";\"%s\"\n", number, MONETARY_AMOUNT_FORMAT.format(price).replace(".", ",")));
				} catch (Exception e) {
					log.info("Error writing line: {}", e.getMessage());
				}
			});
		}

		log.info("Finished writing file: {}", outputFile);
	}

	public void compress(final String inputFilePath, final String gzipFilePath) throws IOException {
		byte[] buffer = new byte[8192];
		int bytesRead;

		try (
			FileInputStream inputStream = new FileInputStream(inputFilePath);
			FileOutputStream outputStream = new FileOutputStream(gzipFilePath);
			GZIPOutputStream gzipOutputStream = new GZIPOutputStream(outputStream)
		){
			while ((bytesRead = inputStream.read(buffer)) != -1) {
				gzipOutputStream.write(buffer, 0, bytesRead);
			}
		}
	}

	protected int getMapSize() {
		return partPricesMap.size();
	}

	private String validatePartNumber(String oemPartNo) {
		if (oemPartNo == null || oemPartNo.length() < 5) {
			return null;
		}

		oemPartNo = oemPartNo.replaceAll("[\\-. ]", "");

		return (PART_NO_PATTERN.matcher(oemPartNo).matches()) ? oemPartNo : null;
	}

	private MonetaryAmount validatePartPrice(String oemPartPrice) {
		if (oemPartPrice == null || oemPartPrice.isEmpty() || !PART_PRICE_PATTERN.matcher(oemPartPrice).matches()) {
			return null;
		}

		oemPartPrice = oemPartPrice.replace(" ", "").replace(",", ".");

		return Money.of(new BigDecimal(oemPartPrice), BG_CCY_UNIT);
	}
}