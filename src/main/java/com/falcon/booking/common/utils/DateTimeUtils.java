package com.falcon.booking.common.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;

public class DateTimeUtils {

    private DateTimeUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static OffsetDateTime toOffsetDateTime(LocalDate date, ZoneId zoneId) {
        if (date == null)
            return null;
        LocalDateTime localDateTime = LocalDateTime.of(date, LocalTime.MIN);
        return localDateTime.atZone(zoneId).toOffsetDateTime();
    }

    public static Map<String, OffsetDateTime> getDayRange(LocalDate date, ZoneId timezone) {
        OffsetDateTime start = date.atStartOfDay(timezone).toOffsetDateTime();
        OffsetDateTime end = date.plusDays(1).atStartOfDay(timezone).toOffsetDateTime();
        Map<String, OffsetDateTime> dayRange = new HashMap<>();
        dayRange.put("start", start);
        dayRange.put("end", end);

        return dayRange;
    }
}
