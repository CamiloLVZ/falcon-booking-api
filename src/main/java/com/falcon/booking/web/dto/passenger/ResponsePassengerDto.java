package com.falcon.booking.web.dto.passenger;

import com.falcon.booking.domain.valueobject.Gender;

import java.time.LocalDate;

public record ResponsePassengerDto(Long id, String firstName, String lastName,
                                   Gender gender, String nationalityIsoCode,
                                   LocalDate dateOfBirth, String passportNumber, String identificationNumber) {
}
