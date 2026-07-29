package com.falcon.booking.feature.catalog.dto;

import com.falcon.booking.feature.airplaneType.dto.AirplaneTypeOptionDto;
import com.falcon.booking.feature.airport.dto.AirportSearchOptionDto;
import com.falcon.booking.feature.country.dto.CountryDto;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record CatalogDropdownDto(
        @Schema(description = "List of airports available for selection")
        List<AirportSearchOptionDto> airports,

        @Schema(description = "List of airplane types available for selection")
        List<AirplaneTypeOptionDto> airplaneTypes,

        @Schema(description = "List of countries available for selection")
        List<CountryDto> countries
) {}