package com.falcon.booking.feature.checkIn.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "DTO representing the check-in request data")
public record CheckInRequestDto(
        
        @Schema(description = "Reservation unique number", example = "ABC123")
        @NotBlank(message = "Reservation number is required")
        String reservationNumber,

        @Schema(description = "Contact email associated with the reservation", example = "contact@example.com")
        @NotBlank(message = "Contact email is required")
        @Email(message = "Contact email must be valid")
        String contactEmail,
        
        @Schema(description = "Passenger identification number", example = "1032456789")
        @NotBlank(message = "Identification number is required")
        String identificationNumber,
        
        @Schema(description = "Country two character ISO code", example = "CO")
        @NotBlank(message = "Country ISO code is required")
        @Size(min = 2, max = 2, message = "Country ISO code must be 2 characters")
        String countryIsoCode,
        
        @Schema(description = "Seat number requested. If not provided, a random one is assigned.", example = "12", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        Integer seatNumber
) {
}
