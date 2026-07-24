package com.falcon.booking.feature.reservation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Data used to verify access to a reservation without authentication")
public record ReservationAccessRequestDto(

        @Schema(description = "Contact email associated with the reservation", example = "contact@example.com")
        @NotBlank(message = "Contact email is required")
        @Email(message = "Contact email must be valid")
        String contactEmail
) {
}
