package com.falcon.booking.web.dto.reservation;

import com.falcon.booking.domain.valueobject.ReservationStatus;
import com.falcon.booking.web.dto.flight.ResponseFlightDto;

import java.time.Instant;
import java.util.List;

public record ResponseReservationDto(
        String number,
        String contactEmail,
        Instant datetimeReservation,
        ReservationStatus status,
        ResponseFlightDto flight,
        List<ResponsePassengerReservationDto> passengers
) { }
