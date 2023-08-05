package com.sopterm.makeawish.common;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import lombok.val;

public class Util {

	private static final float FEE = 3.4f;

	public static int getPriceAppliedFee(int price) {
		return (int)Math.floor(price * (1 - FEE));
	}

	public static int getPricePercent(int totalPrice, int presentPrice) {
		return (getPriceAppliedFee(totalPrice) / presentPrice) * 100;
	}

	public static LocalDateTime convertToDate(String date) {
		return convertToTime(date).toLocalDate().atStartOfDay();
	}

	private static LocalDateTime convertToTime(String dateTime) throws DateTimeParseException {
		val instant = Instant
			.from(DateTimeFormatter.ISO_DATE_TIME.parse(dateTime))
			.atZone(ZoneId.of("Asia/Seoul"));
		return LocalDateTime.from(instant);
	}
}
