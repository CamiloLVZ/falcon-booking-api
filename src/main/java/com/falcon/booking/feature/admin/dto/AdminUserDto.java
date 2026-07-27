package com.falcon.booking.feature.admin.dto;

import com.falcon.booking.feature.passenger.dto.ResponsePassengerDto;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Set;

@Schema(description = "Admin view of a user with roles and optional passenger profile")
public record AdminUserDto(
        @Schema(description = "User numeric unique identifier", example = "1")
        Long id,
        @Schema(description = "User email address", example = "user@example.com")
        String email,
        @Schema(description = "Whether the user account is disabled", example = "false")
        Boolean disabled,
        @Schema(description = "User roles", example = "[\"CLIENT\"]")
        Set<String> roles,
        @Schema(description = "Passenger profile if the user has one")
        ResponsePassengerDto passengerProfile
) {}
