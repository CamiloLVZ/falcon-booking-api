package com.falcon.booking.feature.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(description = "Request to update a user's email and/or password")
public record UpdateUserCredentialsDto(
        @Schema(description = "New email address", example = "newemail@example.com")
        @Size(min = 8, max = 128, message = "email must be an alphanumeric value with 8 to 128 characters")
        String email,
        @Schema(description = "New password", example = "newSecurePassword123")
        @Size(min = 8, max = 128, message = "password must be an alphanumeric value with 8 to 128 characters")
        String password
) {}
