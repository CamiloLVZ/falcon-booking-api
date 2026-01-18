package com.falcon.booking.web.dto.flight;

import java.time.LocalDate;

public record ResponseFlightsGeneratedDto(
        String flightNumber,
        Integer flightsGenerated,
        LocalDate from,
        LocalDate to
) {
}
