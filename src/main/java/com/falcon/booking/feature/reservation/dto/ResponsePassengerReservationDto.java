package com.falcon.booking.feature.reservation.dto;

import com.falcon.booking.common.enums.PassengerReservationStatus;
import com.falcon.booking.common.enums.SeatClass;
import com.falcon.booking.feature.passenger.dto.ResponsePassengerDto;
import io.swagger.v3.oas.annotations.media.Schema;

public record ResponsePassengerReservationDto (
        @Schema(description = "Passenger reservation internal ID", example = "100")
        Long id,
        @Schema(description = "Passenger data")
        ResponsePassengerDto passenger,
        @Schema(description = "Seat number assigned to the passenger", example = "12")
        Integer seatNumber,
        @Schema(description = "Human-readable seat label (e.g. 12A)", example = "2C")
        String seatLabel,
        @Schema(description = "Seat class selected (FIRST_CLASS or ECONOMY)", example = "ECONOMY")
        SeatClass seatClass,
        @Schema(description = "Passenger reservation status", example = "CHECKED_IN")
        PassengerReservationStatus status
){ }