package com.falcon.booking.web.dto.passenger;

import com.falcon.booking.domain.valueobject.PassengerGender;

import java.time.LocalDate;

public record ResponsePassengerDto(Long id, String firstName, String lastName,
                                   PassengerGender gender, String nationalityIsoCode,
                                   LocalDate dateOfBirth, String passportNumber, String identificationNumber) {
}
