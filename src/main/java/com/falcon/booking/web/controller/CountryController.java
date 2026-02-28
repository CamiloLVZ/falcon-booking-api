package com.falcon.booking.web.controller;

import com.falcon.booking.domain.service.AirportService;
import com.falcon.booking.domain.service.CountryService;
import com.falcon.booking.web.dto.AirportDto;
import com.falcon.booking.web.dto.CountryDto;
import com.falcon.booking.web.exception.Error;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Size;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Countries", description = "Operations related to countries")
@RestController
@RequestMapping("/v1/countries")
@Validated
public class CountryController {

    private final CountryService countryService;
    private final AirportService airportService ;

    @Autowired
    public CountryController(CountryService countryService, AirportService airportService) {
        this.countryService = countryService;
        this.airportService = airportService;
    }

    @Operation(summary = "Get a country by its iso Code",
            description = "Returns a country record using its unique two characters ISO code.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Country retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = CountryDto.class))),
            @ApiResponse(responseCode = "400", description = "Error by invalid ISO code format",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "404", description = "Country not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class)))
    })
    @GetMapping("/{isoCode}")
    public ResponseEntity<CountryDto> getCountry(@PathVariable @Size(min = 2, max = 2,  message = "Iso Code must be a String with 2 characters")
                                                     @Parameter(description = "Country two character ISO code", example = "CO")
                                                     String isoCode) {
        CountryDto country = countryService.getCountryByIsoCode(isoCode);
        return ResponseEntity.ok(country);
    }

    @Operation(summary = "Get all countries",
            description = "Returns a list with all registered countries.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Country list retrieved successfully, even if it is empty",
                    content = @Content(mediaType = "application/json", array = @ArraySchema (schema = @Schema(implementation = CountryDto.class))))
    })
    @GetMapping
    public ResponseEntity<List<CountryDto>> getAllCountries() {
        List<CountryDto> countries = countryService.getAllCountries();
        return ResponseEntity.ok(countries);
    }


    @Operation(summary = "Get all the airports of a country",
            description = "Returns a list with all the airports related to a country using its unique two characters ISO code.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Airport list retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = CountryDto.class))),
            @ApiResponse(responseCode = "400", description = "Error by invalid iso code format",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "404", description = "Country not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class)))
    })
    @GetMapping("/{isoCode}/airports")
    public ResponseEntity<List<AirportDto>> getAirportsByCountryIsoCode(@PathVariable @Size(min = 2, max = 2)
                                                                            @Parameter(description = "Country two character ISO code", example = "CO")
                                                                            String isoCode) {

        return ResponseEntity.ok(airportService.getAirportsByCountryIsoCode(isoCode));
    }

}
