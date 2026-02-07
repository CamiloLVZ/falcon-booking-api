package com.falcon.booking.web.dto.passenger;

import com.falcon.booking.domain.valueobject.PassengerGender;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record AddPassengerDto(
        @NotBlank @Size(min = 1, max = 100)
        String firstName,
        @NotBlank @Size(min = 1, max = 100)
        String lastName,
        PassengerGender gender,
        @NotBlank @Size(min = 2, max = 2)
        String nationalityIsoCode,
        @NotNull @Past
        LocalDate dateOfBirth,
        @Size(min = 2)
        String passportNumber,
        @NotBlank @Size(min = 2, max = 20)
        String identificationNumber){
}
