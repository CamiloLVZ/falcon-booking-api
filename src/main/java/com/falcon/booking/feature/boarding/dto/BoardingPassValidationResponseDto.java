package com.falcon.booking.feature.boarding.dto;

import com.falcon.booking.common.enums.BoardingPassStatus;
import com.falcon.booking.common.enums.SeatClass;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;
import java.util.UUID;

public record BoardingPassValidationResponseDto(
        UUID qrToken,
        String passengerName,
        String identification,
        String flightNumber,
        String origin,
        String destination,
        OffsetDateTime departureTime,
        SeatClass seatClass,
        Integer seatNumber,
        @Schema(description = "Human-readable seat label (e.g. 12A)", example = "12A")
        String seatLabel,
        BoardingPassStatus status
) {
}
