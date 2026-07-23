package com.falcon.booking.feature.airplaneType.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ConfigureSeatsDto(

        @Schema(description = "Number of economy seats", example = "150")
        @Min(value = 0, message = "The number of economy seats cannot be negative")
        int economySeats,

        @Schema(description = "Number of first class seats", example = "20")
        @Min(value = 0, message = "The number of first class seats cannot be negative")
        int firstClassSeats,

        @Schema(description = "Seat column letters (uppercase, no duplicates, e.g. 'ABCDEF')", example = "ABCDEF")
        @NotBlank(message = "Seat columns cannot be empty")
        @Pattern(regexp = "[A-Z]+", message = "Seat columns must contain only uppercase letters")
        @Size(max = 10, message = "Seat columns cannot exceed 10 characters")
        String seatColumns

) { }
