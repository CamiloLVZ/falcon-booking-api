package com.falcon.booking.web.dto.reservation;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.List;

public record AddReservationDto(
        @Schema(description = "Flight numeric identifier for the reservation", example = "100")
        @NotNull @Positive
        Long idFlight,
        @Schema(description = "Contact email for the reservation", example = "contact@example.com")
        @NotNull @Email
        String contactEmail,
        @ArraySchema(schema = @Schema(implementation = AddPassengerReservationDto.class),
                minItems = 1,
                maxItems = 3,
                arraySchema = @Schema(description = "Passenger-seat assignments included in reservation"))
        @NotNull @Size(min = 1, max = 3)
        List<AddPassengerReservationDto> passengers
) { }