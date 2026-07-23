package com.falcon.booking.feature.boardingPass.dto;

public record BoardingPassView(
        String passengerName,
        String flightNumber,
        String originAirport,
        String destinationAirport,
        DateTimeView departure,
        DateTimeView boardingStart,
        DateTimeView boardingEnd,
        String seat,
        String reservationNumber,
        String qr,
        String logo
) { }
