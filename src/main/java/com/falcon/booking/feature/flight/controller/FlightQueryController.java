package com.falcon.booking.feature.flight.controller;

import com.falcon.booking.common.enums.FlightStatus;
import com.falcon.booking.common.web.Error;
import com.falcon.booking.common.web.PagedResponse;
import com.falcon.booking.feature.flight.dto.ResponseFlightDto;
import com.falcon.booking.feature.flight.service.FlightQueryService;
import com.falcon.booking.feature.flightGeneration.dto.ResponseFlightsGenerationDto;
import com.falcon.booking.feature.flightGeneration.service.FlightGenerationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "Flight Queries", description = "Operations related to flight querying")
@RestController
@RequestMapping("/v1/flights")
@Validated
public class FlightQueryController {

    private final FlightQueryService flightQueryService;
    private final FlightGenerationService  flightGenerationService;

    @Autowired
    public FlightQueryController(FlightQueryService flightQueryService, FlightGenerationService flightGenerationService) {
        this.flightQueryService = flightQueryService;
        this.flightGenerationService = flightGenerationService;
    }

    @Operation(summary = "Get a flight by id",
            description = "Returns a flight record using its unique numeric identifier.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Flight retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseFlightDto.class))),
            @ApiResponse(responseCode = "400", description = "Error by invalid id argument",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "404", description = "Flight not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<ResponseFlightDto> getFlightById(@PathVariable
                                                           @Parameter(description = "Flight numeric unique identifier", example = "100")
                                                           Long id) {
        return ResponseEntity.ok(flightQueryService.getFlightById(id));
    }

    @Operation(summary = "Get flights by criteria",
            description = "Returns a paginated list of flights by route flight number with optional status and date range filters.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Paginated flights retrieved successfully, even if content is empty",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = PagedResponse.class))),
            @ApiResponse(responseCode = "400", description = "Error by invalid query or pagination parameters",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class)))
    })
    @GetMapping
    public ResponseEntity<PagedResponse<ResponseFlightDto>> getAllFlights(@RequestParam @NotNull
                                                                 @Size(min = 5, max = 7, message = "Flight number must be an alphanumeric value with 5 to 7 characters")
                                                                 @Parameter(description = "Route flight number", example = "AV1234")
                                                                 String flightNumber,
                                                                 @RequestParam(required = false)
                                                                 @Parameter(description = "Flight status", example = "SCHEDULED")
                                                                 FlightStatus status,
                                                                 @RequestParam(required = false)
                                                                 @Parameter(description = "Initial date for filtering flights", example = "2026-02-01")
                                                                 LocalDate dateFrom,
                                                                 @RequestParam(required = false)
                                                                 @Parameter(description = "Final date for filtering flights", example = "2026-02-28")
                                                                 LocalDate dateTo,
                                                                 @RequestParam @Min(1) @NotNull
                                                                 @Parameter(description = "Number of flight records to be returned per page", example = "10", required = true)
                                                                 int size,
                                                                 @RequestParam @Min(0) @NotNull
                                                                 @Parameter(description = "Zero-based page number to be returned", example = "0", required = true)
                                                                 int page
    ) {
        Page<ResponseFlightDto> flights = flightQueryService.getAllFlights(flightNumber, status, dateFrom, dateTo, page, size);
        return ResponseEntity.ok(PagedResponse.from(flights));
    }


    @Operation(summary = "Search flights by route and date",
            description = "Returns a list of flights available for a given origin airport, destination airport, and departure date.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Flights retrieved successfully, even if content is empty",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseFlightDto.class))),
            @ApiResponse(responseCode = "400", description = "Error by invalid query parameters",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class)))
    })
    @GetMapping("/search")
    public ResponseEntity<List<ResponseFlightDto>> getFlightsByAirportsAndDate(@RequestParam @Size(min = 3, max = 3, message = "origin must be an airports Iata Code (3 letter String)")
                                                                            @Parameter(description = "Origin airport IATA code", example = "BOG")
                                                                            String origin,
                                                                            @RequestParam @Size(min = 3, max = 3, message = "destination must be an airports Iata Code (3 letter String)")
                                                                            @Parameter(description = "Destination airport IATA code", example = "MIA")
                                                                            String destination,
                                                                            @RequestParam
                                                                            @Parameter(description = "Flight departure date", example = "2026-02-20")
                                                                            LocalDate date,
                                                                            @RequestParam(required = false)
                                                                            @Parameter(description = "Status of flights to search", example = "SCHEDULED")
                                                                            FlightStatus status) {
        List<ResponseFlightDto> flights = flightQueryService.getAllFlightsByOriginDestinationAndDate(origin, destination, date, status);
        return ResponseEntity.ok(flights);
    }

    @Operation(summary = "Get all flight generations",
            description = "Returns a paginated list of historic flight generations. Requires authentication with JWT token and ADMIN role",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Paginated flight generations retrieved successfully, even if content is empty",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = PagedResponse.class))),
            @ApiResponse(responseCode = "400", description = "Error by invalid pagination parameters",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions to retrieve flight generations",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class)))
    })
    @GetMapping("/generations")
    public ResponseEntity<PagedResponse<ResponseFlightsGenerationDto>> getAllFlightGenerations(@RequestParam @Min(1) @NotNull
                                                                                               @Parameter(description = "Number of flight generation records to be returned per page", example = "10", required = true)
                                                                                               int size,
                                                                                               @RequestParam @Min(0) @NotNull
                                                                                               @Parameter(description = "Zero-based page number to be returned", example = "0", required = true)
                                                                                               int page) {
        Page<ResponseFlightsGenerationDto> generations = flightGenerationService.getAllFlightGenerations(page, size);
        return ResponseEntity.ok(PagedResponse.from(generations));
    }

    @Operation(summary = "Get a flight generation by id",
            description = "Returns a flight generation process record using its unique numeric identifier. Requires authentication with JWT token and ADMIN role",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Flight generation retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseFlightsGenerationDto.class))),
            @ApiResponse(responseCode = "400", description = "Error by invalid id argument",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions to retrieve flight generations",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "404", description = "Flight generation not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class)))
    })
    @GetMapping("/generations/{id}")
    public ResponseEntity<ResponseFlightsGenerationDto> getFlightsGeneration(@PathVariable
                                                                             @Parameter(description = "Flights generation unique identifier.", example = "10")
                                                                             Long id) {
        return ResponseEntity.ok(flightGenerationService.getFlightGeneration(id));
    }

}
