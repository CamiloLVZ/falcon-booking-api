package com.falcon.booking.feature.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record PasswordResetRequestDto(
        @NotBlank(message = "Email can not be blank")
        @Email(message = "Email must be valid")
        String email
) {
}
