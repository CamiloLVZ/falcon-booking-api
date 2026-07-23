package com.falcon.booking.feature.boardingPass.dto;

import java.time.LocalDateTime;

public record BoardingPassDocumentData(
        String passengerName,
        String flightNumber,
        String originAirport,
        String destinationAirport,
        LocalDateTime departureTime,
        LocalDateTime boardingStartTime,
        LocalDateTime boardingEndTime,
        String zone,
        String seat,
        String reservationNumber,
        byte[] qr
) {
}
