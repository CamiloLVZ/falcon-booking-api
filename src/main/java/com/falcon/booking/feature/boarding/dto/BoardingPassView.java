package com.falcon.booking.feature.boarding.dto;

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
