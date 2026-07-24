package com.falcon.booking.feature.payment.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record PaymentRequestDto(
    @NotNull(message = "Flight id can not be null")
    Long flightId,

    @NotBlank(message = "Contact email can not be blank")
    @Email(message = "Contact email must be valid")
    String contactEmail,

    @NotNull(message = "Passengers list can not be null")
    @Size(min = 1, message = "Reservation must have at least one passenger")
    @Valid
    List<PaymentPassengerDto> passengers
) {}
