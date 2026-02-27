package com.falcon.booking.web.dto.reservation;

import com.falcon.booking.domain.valueobject.ReservationStatus;
import com.falcon.booking.web.dto.flight.ResponseFlightDto;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;

public record ResponseReservationDto(
        @Schema(description = "Reservation unique number", example = "ABC123")
        String number,
        @Schema(description = "Contact email for the reservation", example = "contact@example.com")
        String contactEmail,
        @Schema(description = "Reservation creation date and time UTC", example = "2026-02-10T15:20:30Z")
        Instant datetimeReservation,
        @Schema(description = "Current reservation status", example = "CONFIRMED")
        ReservationStatus status,
        @Schema(description = "Flight associated with reservation")
        ResponseFlightDto flight,
        @ArraySchema(schema = @Schema(implementation = ResponsePassengerReservationDto.class),
                arraySchema = @Schema(description = "Passengers linked to this reservation"))
        List<ResponsePassengerReservationDto> passengers
) { }