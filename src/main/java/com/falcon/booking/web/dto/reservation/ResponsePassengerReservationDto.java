package com.falcon.booking.web.dto.reservation;

import com.falcon.booking.domain.valueobject.PassengerReservationStatus;
import com.falcon.booking.web.dto.passenger.ResponsePassengerDto;
import io.swagger.v3.oas.annotations.media.Schema;

public record ResponsePassengerReservationDto (
        @Schema(description = "Passenger data")
        ResponsePassengerDto passenger,
        @Schema(description = "Seat number assigned to the passenger", example = "12")
        Integer seatNumber,
        @Schema(description = "Passenger reservation status", example = "CHECKED_IN")
        PassengerReservationStatus status
){ }