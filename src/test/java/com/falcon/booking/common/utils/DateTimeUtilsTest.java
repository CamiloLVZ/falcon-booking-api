package com.falcon.booking.common.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class DateTimeUtilsTest {

    @Test
    @DisplayName("Should convert LocalDate to OffsetDateTime at start of day")
    void shouldConvertLocalDateToOffsetDateTime() {
        LocalDate date = LocalDate.of(2026, 6, 27);
        ZoneId zoneId = ZoneId.of("America/Bogota"); // UTC-5
        
        OffsetDateTime result = DateTimeUtils.toOffsetDateTime(date, zoneId);
        
        assertNotNull(result);
        assertEquals(2026, result.getYear());
        assertEquals(6, result.getMonthValue());
        assertEquals(27, result.getDayOfMonth());
        assertEquals(0, result.getHour());
        assertEquals(0, result.getMinute());
        assertEquals(ZoneOffset.ofHours(-5), result.getOffset());
    }

    @Test
    @DisplayName("Should return null when converting null LocalDate")
    void shouldReturnNullWhenConvertingNullLocalDate() {
        assertNull(DateTimeUtils.toOffsetDateTime(null, ZoneId.of("UTC")));
    }

    @Test
    @DisplayName("Should return day range map")
    void shouldReturnDayRangeMap() {
        LocalDate date = LocalDate.of(2026, 6, 27);
        ZoneId zoneId = ZoneId.of("UTC");
        
        Map<String, OffsetDateTime> range = DateTimeUtils.getDayRange(date, zoneId);
        
        assertNotNull(range);
        assertTrue(range.containsKey("start"));
        assertTrue(range.containsKey("end"));
        
        OffsetDateTime start = range.get("start");
        OffsetDateTime end = range.get("end");
        
        assertEquals(2026, start.getYear());
        assertEquals(27, start.getDayOfMonth());
        assertEquals(0, start.getHour());
        
        assertEquals(2026, end.getYear());
        assertEquals(28, end.getDayOfMonth()); // next day
        assertEquals(0, end.getHour());
    }
}
