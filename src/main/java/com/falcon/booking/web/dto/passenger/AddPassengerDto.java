package com.falcon.booking.web.dto.passenger;

import com.falcon.booking.domain.valueobject.PassengerGender;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record AddPassengerDto(
        @Schema(description = "Passenger first name", example = "Juan")
        @NotBlank @Size(min = 1, max = 100)
        String firstName,
        @Schema(description = "Passenger last name", example = "Perez")
        @NotBlank @Size(min = 1, max = 100)
        String lastName,
        @Schema(description = "Passenger gender", example = "MALE")
        PassengerGender gender,
        @Schema(description = "Passenger nationality ISO code", example = "CO")
        @NotBlank @Size(min = 2, max = 2)
        String nationalityIsoCode,
        @Schema(description = "Passenger birth date", example = "1995-07-16")
        @NotNull @Past
        LocalDate dateOfBirth,
        @Schema(description = "Passenger passport number", example = "A1234567")
        @Size(min = 2)
        String passportNumber,
        @Schema(description = "Passenger identification number", example = "1032456789")
        @NotBlank @Size(min = 2, max = 20)
        String identificationNumber){
}