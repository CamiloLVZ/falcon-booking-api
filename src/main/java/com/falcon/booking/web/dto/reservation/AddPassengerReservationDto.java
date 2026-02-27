package com.falcon.booking.web.dto.reservation;

import com.falcon.booking.web.dto.passenger.AddPassengerDto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record AddPassengerReservationDto(
        @Schema(description = "Passenger data related to the reservation")
        @NotNull
        AddPassengerDto passenger,

        @Schema(description = "Seat number assigned to the passenger", example = "12")
        @NotNull @Positive
        Integer seatNumber
) { }