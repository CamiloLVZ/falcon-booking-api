package com.falcon.booking.web.dto.reservation;

import com.falcon.booking.web.dto.flight.ResponseFlightDto;

import java.time.Instant;
import java.util.List;

public record ResponseReservationDto(
        String number,
        String contactEmail,
        Instant datetimeReservation,
        ResponseFlightDto flight,
        List<ResponsePassengerReservationDto> passengers
) { }
