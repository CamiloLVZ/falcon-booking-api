package com.falcon.booking.feature.reservation.dto;

import com.falcon.booking.common.enums.SeatClass;
import com.falcon.booking.feature.passenger.dto.AddPassengerDto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record AddPassengerReservationDto(
        @Schema(description = "Passenger data related to the reservation")
        @NotNull
        AddPassengerDto passenger,

        @Schema(description = "Seat number selected (optional)", example = "12")
        @Positive
        Integer seatNumber,

        @Schema(description = "Seat class selected (FIRST_CLASS or ECONOMY)", example = "ECONOMY")
        @NotNull
        SeatClass seatClass
) { }