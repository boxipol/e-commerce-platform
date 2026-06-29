package com.pd.ecommerce.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class SkuGenerator {

	public static String generate(String brand, String name, String description) {
		String brandCode = brand.substring(0, 3).toUpperCase(); // Apple → APL
		String modelCode = name.replaceAll("[^A-Za-z0-9]", "").toUpperCase(); // iPhone6 → IPHONE6
		String storage = extract(description, "\\d+GB", "NA").toUpperCase();
		String color = extractColor(description);

		return brandCode + "-" + modelCode + "-" + storage + "-" + color;
	}

//	==================== PRIVATE ====================

	private static String extract(String text, String regex, String fallback) {
		Matcher m = Pattern.compile(regex, Pattern.CASE_INSENSITIVE).matcher(text);
		return m.find() ? m.group() : fallback;
	}

	private static String extractColor(String description) {
		if (description.toLowerCase().contains("silver")) {
			return "S";
		}

		if (description.toLowerCase().contains("black")) {
			return "B";
		}

		if (description.toLowerCase().contains("white")) {
			return "W";
		}

		return "X";
	}
}