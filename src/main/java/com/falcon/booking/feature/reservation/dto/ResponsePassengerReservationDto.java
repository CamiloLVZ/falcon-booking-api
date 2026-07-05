package com.falcon.booking.feature.reservation.dto;

import com.falcon.booking.common.enums.PassengerReservationStatus;
import com.falcon.booking.feature.passenger.dto.ResponsePassengerDto;
import io.swagger.v3.oas.annotations.media.Schema;

public record ResponsePassengerReservationDto (
        @Schema(description = "Passenger data")
        ResponsePassengerDto passenger,
        @Schema(description = "Seat number assigned to the passenger", example = "12")
        Integer seatNumber,
        @Schema(description = "Passenger reservation status", example = "CHECKED_IN")
        PassengerReservationStatus status
){ }