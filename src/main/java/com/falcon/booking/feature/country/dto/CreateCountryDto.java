package com.falcon.booking.feature.country.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Request to create a new country")
public record CreateCountryDto(
        @Schema(description = "Country common name", example = "Colombia")
        @NotBlank(message = "Country name can not be blank")
        @Size(max = 100, message = "Country name must be at most 100 characters")
        String name,
        @Schema(description = "Country two character ISO code", example = "CO")
        @NotBlank(message = "ISO code can not be blank")
        @Size(min = 2, max = 2, message = "ISO code must be exactly 2 characters")
        String isoCode
) {}
