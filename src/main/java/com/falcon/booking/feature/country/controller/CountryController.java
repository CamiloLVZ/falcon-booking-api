package com.falcon.booking.feature.country.controller;

import com.falcon.booking.common.web.Error;
import com.falcon.booking.common.web.PagedResponse;
import com.falcon.booking.feature.airport.dto.AirportDto;
import com.falcon.booking.feature.airport.service.AirportService;
import com.falcon.booking.feature.country.dto.CountryDto;
import com.falcon.booking.feature.country.dto.CreateCountryDto;
import com.falcon.booking.feature.country.service.CountryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Countries", description = "Operations related to countries")
@RestController
@RequestMapping("/v1/countries")
@Validated
public class CountryController {

    private final CountryService countryService;
    private final AirportService airportService;

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
    public ResponseEntity<CountryDto> getCountry(@PathVariable @Size(min = 2, max = 2, message = "Iso Code must be a String with 2 characters")
                                                 @Parameter(description = "Country two character ISO code", example = "CO")
                                                 String isoCode) {
        CountryDto country = countryService.getCountryByIsoCode(isoCode);
        return ResponseEntity.ok(country);
    }

    @Operation(summary = "Get all countries",
            description = "Returns a list with all registered countries.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Country list retrieved successfully, even if it is empty",
                    content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = CountryDto.class))))
    })
    @GetMapping
    public ResponseEntity<List<CountryDto>> getAllCountries() {
        List<CountryDto> countries = countryService.getAllCountries();
        return ResponseEntity.ok(countries);
    }


    @Operation(summary = "Create a new country", description = "Creates a new country record. Requires ADMIN role.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Country created successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = CountryDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request body",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "409", description = "Country with this ISO code already exists",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class)))
    })
    @PostMapping
    public ResponseEntity<CountryDto> createCountry(@Valid @RequestBody
                                                     @io.swagger.v3.oas.annotations.parameters.RequestBody(
                                                             description = "Country creation data",
                                                             required = true)
                                                     CreateCountryDto dto) {
        CountryDto created = countryService.createCountry(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Operation(summary = "Get airports of a country",
            description = "Returns a paginated list with all the airports related to a country using its unique two characters ISO code.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Paginated airport list retrieved successfully, even if content is empty",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = PagedResponse.class))),
            @ApiResponse(responseCode = "400", description = "Error by invalid ISO code format or pagination parameters",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "404", description = "Country not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class)))
    })
    @GetMapping("/{isoCode}/airports")
    public ResponseEntity<PagedResponse<AirportDto>> getAirportsByCountryIsoCode(@PathVariable @Size(min = 2, max = 2)
                                                                                 @Parameter(description = "Country two character ISO code", example = "CO")
                                                                                 String isoCode, @RequestParam @Min(1) @NotNull
                                                                                 @Parameter(description = "Number of airport records to be returned per page", example = "10", required = true)
                                                                                 int size,
                                                                                 @RequestParam @Min(0) @NotNull
                                                                                 @Parameter(description = "Zero-based page number to be returned", example = "0", required = true)
                                                                                 int page) {
        Page<AirportDto> airports = airportService.getAirportsByCountryIsoCode(isoCode, page, size);
        return ResponseEntity.ok(PagedResponse.from(airports));
    }

}
