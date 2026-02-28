package com.falcon.booking.web.dto.passenger;

import com.falcon.booking.domain.valueobject.PassengerGender;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

public record ResponsePassengerDto(
        @Schema(description = "Passenger numeric unique identifier", example = "25")
        Long id,
        @Schema(description = "Passenger first name", example = "Juan")
        String firstName,
        @Schema(description = "Passenger last name", example = "Perez")
        String lastName,
        @Schema(description = "Passenger gender", example = "MALE")
        PassengerGender gender,
        @Schema(description = "Passenger nationality ISO code", example = "CO")
        String nationalityIsoCode,
        @Schema(description = "Passenger birth date", example = "1995-07-16")
        LocalDate dateOfBirth,
        @Schema(description = "Passenger passport number", example = "A1234567")
        String passportNumber,
        @Schema(description = "Passenger identification number", example = "1032456789")
        String identificationNumber) {
}