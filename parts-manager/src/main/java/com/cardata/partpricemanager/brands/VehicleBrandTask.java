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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.zip.GZIPOutputStream;

/**
 * Created by Dobrev-DAT on 2.2.2016 г..
 */
@Slf4j
@Getter
public abstract sealed class VehicleBrandTask implements Runnable permits BMW, FORD, JAGUAR, OPEL, PORSCHE, RENAULT, ROVER, SKODA, TOYOTA, VOLVO {

	public static final LocalDate CURRENT_DATE = LocalDate.now();
	public static final String WORKING_DIR = String.format("%s/parts-manager/PriceLists/%s/%s/", System.getProperty("user.dir"), CURRENT_DATE.getYear(), CURRENT_DATE.getMonthValue());

	private static final Pattern PART_NO_PATTERN = Pattern.compile("[a-zA-Z0-9/]+");
	private static final Pattern PART_PRICE_PATTERN = Pattern.compile("[0-9., ]+");
	private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy_MM_dd");
	private static final CurrencyUnit BG_CCY_UNIT = Monetary.getCurrency("BGN");
	private static final MonetaryAmountFormat MONETARY_AMOUNT_FORMAT = MonetaryFormats.getAmountFormat(AmountFormatQueryBuilder
		.of(Locale.ENGLISH)
		.set(CurrencyUnit.class, BG_CCY_UNIT)
		.set("pattern", "0.00")
		.build());

	private final Map<String, MonetaryAmount> partPricesMap;

	protected static final Charset DEFAULT_OUTPUT_ENCODING = StandardCharsets.US_ASCII;
	protected static final String CURRENT_DATE_STR = DATE_TIME_FORMATTER.format(CURRENT_DATE);
	protected static final String ARCHIVE_EXT = ".csv.gz";
	protected static final String TAB = "\\t";

	protected final Brand brand;
	protected final Path inputFile;
	protected final Path outputFile;
	protected final Path zipFile;
	protected final Path logFile;

	protected int errorLines = 0;


	public VehicleBrandTask(final Brand brand) throws Exception {
		this.brand = brand;
		var sourceFolderPath = Paths.get(String.format("%s/%s", WORKING_DIR, brand));

		try (var files = Files.list(sourceFolderPath)) {
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
				.orElseThrow(() -> new IOException("Source file not found: " + brand));
		}

		outputFile = Path.of(String.format("%s%s/%s_output.txt", WORKING_DIR, brand, brand));
		zipFile = Path.of(String.format("%s%s/DAT.BG_%s_%s_retail_exvat_%s%s", WORKING_DIR, brand, brand, brand.getBrandCode(), CURRENT_DATE_STR, ARCHIVE_EXT));
		logFile = Path.of(String.format("%s%s/%s_log.txt", WORKING_DIR, brand, brand));
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

	public void writeOutputFile(final Path outputFile) throws IOException {
		log.info("Writing file: {}", outputFile);

		try (
			var output = new FileOutputStream(outputFile.toString());
			var writer = new OutputStreamWriter(output, DEFAULT_OUTPUT_ENCODING)
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

	public void compress(final Path inputFilePath, final Path gzipFilePath) throws IOException {
		var buffer = new byte[8192];
		int bytesRead;

		try (
			var input = new FileInputStream(inputFilePath.toString());
			var output = new FileOutputStream(gzipFilePath.toString());
			var gzipOutput = new GZIPOutputStream(output)
		){
			while ((bytesRead = input.read(buffer)) != -1) {
				gzipOutput.write(buffer, 0, bytesRead);
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