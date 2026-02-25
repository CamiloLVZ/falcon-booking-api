package com.falcon.booking.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record CountryDto(
        @Schema(description = "Country common name", example = "Colombia")
        String name,
        @Schema(description = "Country two character ISO code", example = "CO")
        String isoCode
) { }
