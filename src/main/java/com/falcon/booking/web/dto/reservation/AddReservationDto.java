package com.falcon.booking.web.dto.reservation;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.List;

public record AddReservationDto(
        @NotNull @Positive
        Long idFlight,
        @NotNull @Email
        String contactEmail,
        @NotNull @Size(min = 1, max = 3)
        List<AddPassengerReservationDto> passengers
) { }
