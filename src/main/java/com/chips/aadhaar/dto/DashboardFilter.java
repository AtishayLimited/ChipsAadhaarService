package com.chips.aadhaar.dto;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public record DashboardFilter(LocalDateTime start, LocalDateTime endExclusive, String gateway, String errorCode) {
	public static DashboardFilter of(LocalDate from, LocalDate to, LocalTime fromTime, LocalTime toTime, String gateway,
			String errorCode, ZoneId zone) {
		LocalDate today = LocalDate.now(zone);
		if (from == null)
			from = today.minusDays(29);
		if (to == null)
			to = today;
		LocalDateTime start = from.atTime(fromTime == null ? LocalTime.MIDNIGHT : fromTime);
		LocalDateTime end = toTime == null ? to.plusDays(1).atStartOfDay() : to.atTime(toTime).plusMinutes(1);
		if (!start.isBefore(end) || ChronoUnit.DAYS.between(start, end) > 366)
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					"Date range must be ordered and at most 366 days");
		if ((fromTime != null && (fromTime.getSecond() != 0 || fromTime.getNano() != 0))
				|| (toTime != null && (toTime.getSecond() != 0 || toTime.getNano() != 0)))
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Use time format HH:mm");
		gateway = clean(gateway);
		errorCode = clean(errorCode);
		if (gateway != null) {
			gateway = gateway.toUpperCase(Locale.ROOT);
			if (gateway.length() > 255)
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Gateway exceeds database length");
		}
		if (errorCode != null && errorCode.length() > 5)
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Error code exceeds database length");
		return new DashboardFilter(start, end, gateway, errorCode);
	}

	private static String clean(String s) {
		return s == null || s.isBlank() || s.equalsIgnoreCase("ALL") ? null : s.trim();
	}
}
