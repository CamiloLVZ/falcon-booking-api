package com.falcon.booking.web.dto.reservation;

import com.falcon.booking.web.dto.passenger.AddPassengerDto;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record AddPassengerReservationDto(
        @NotNull
        AddPassengerDto passenger,

        @NotNull @Positive
        Integer seatNumber
) { }
