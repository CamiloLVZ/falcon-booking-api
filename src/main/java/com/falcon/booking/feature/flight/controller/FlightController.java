package com.falcon.booking.feature.flight.controller;

import com.falcon.booking.feature.flight.service.FlightService;
import com.falcon.booking.common.enums.FlightStatus;
import com.falcon.booking.feature.flight.dto.CreateFlightDto;
import com.falcon.booking.feature.flight.dto.ResponseFlightDto;
import com.falcon.booking.feature.flightGeneration.dto.ResponseFlightsGenerationDto;
import com.falcon.booking.common.web.Error;
import com.falcon.booking.common.web.PagedResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Tag(name = "Flights", description = "Operations related to flight management")
@RestController
@RequestMapping("/v1/flights")
@Validated
public class FlightController {

    private final FlightService flightService;

    @Autowired
    public FlightController(FlightService flightService) {
        this.flightService = flightService;
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
        return ResponseEntity.ok(flightService.getFlightById(id));
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
        Page<ResponseFlightDto> flights = flightService.getAllFlights(flightNumber, status, dateFrom, dateTo, page, size);
        return ResponseEntity.ok(PagedResponse.from(flights));
    }

    @Operation(summary = "Reschedule a flight",
            description = "Updates departure date and time for a flight that can still be rescheduled. Requires authentication with JWT token and ADMIN role",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Flight rescheduled successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseFlightDto.class))),
            @ApiResponse(responseCode = "400", description = "Error by invalid arguments or invalid reschedule state",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions to reschedule flights",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "404", description = "Flight not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class)))
    })
    @PostMapping("/{id}/reschedule")
    public ResponseEntity<ResponseFlightDto> rescheduleFlight(@PathVariable
                                                              @Parameter(description = "Flight numeric unique identifier", example = "100")
                                                              Long id,
                                                              @RequestParam @Future
                                                              @Parameter(description = "New local departure date time", example = "2026-02-20T14:30:00")
                                                              LocalDateTime newDepartureLocalDateTime) {
        return ResponseEntity.status(HttpStatus.CREATED).body(flightService.rescheduleFLight(id, newDepartureLocalDateTime));
    }

    @Operation(summary = "Create a new flight",
            description = "Creates a new flight for an existing route and departure date time. Requires authentication with JWT token and ADMIN role",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Flight created successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseFlightDto.class))),
            @ApiResponse(responseCode = "400", description = "Error by invalid request body",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions to create flights",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "404", description = "Route not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class)))
    })
    @PostMapping
    public ResponseEntity<ResponseFlightDto> addFlight(@RequestBody @Valid
                                                       @io.swagger.v3.oas.annotations.parameters.RequestBody(
                                                               description = "Data for creating a new flight",
                                                               required = true)
                                                       CreateFlightDto createFlightDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(flightService.addFlight(createFlightDto));
    }

    @Operation(summary = "Cancel a flight",
            description = "Changes flight status to CANCELED when cancellation is allowed. Requires authentication with JWT token and ADMIN role",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Flight canceled successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseFlightDto.class))),
            @ApiResponse(responseCode = "400", description = "Error by invalid flight state",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions to cancel flights",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "404", description = "Flight not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class)))
    })
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<ResponseFlightDto> cancelFlight(@PathVariable
                                                          @Parameter(description = "Flight numeric unique identifier", example = "100")
                                                          Long id) {
        return ResponseEntity.ok(flightService.cancelFlight(id));
    }

    @Operation(summary = "Change flight airplane type",
            description = "Replaces airplane type assigned to a flight using the airplane type identifier. Requires authentication with JWT token and ADMIN role",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Flight airplane type changed successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseFlightDto.class))),
            @ApiResponse(responseCode = "400", description = "Error by invalid arguments or invalid flight state",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions to change flight airplane type",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "404", description = "Flight or airplane type not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class)))
    })
    @PatchMapping("/{id}/change-airplane-type")
    public ResponseEntity<ResponseFlightDto> changeAirplaneType(@PathVariable Long id,
                                                                @Parameter(description = "Airplane type numeric unique identifier", example = "10")
                                                                @RequestParam Long idAirplaneType) {
        return ResponseEntity.ok(flightService.changeAirplaneType(id, idAirplaneType));
    }

    @Operation(summary = "Search flights by route and date",
            description = "Returns a paginated list of flights available for a given origin airport, destination airport, and departure date.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Paginated flights retrieved successfully, even if content is empty",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = PagedResponse.class))),
            @ApiResponse(responseCode = "400", description = "Error by invalid query or pagination parameters",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class)))
    })
    @GetMapping("/search")
    public ResponseEntity<PagedResponse<ResponseFlightDto>> getFlightsByAirportsAndDate(@RequestParam @Size(min = 3, max = 3, message = "origin must be an airports Iata Code (3 letter String)")
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
                                                                            FlightStatus status,
                                                                            @RequestParam @Min(1) @NotNull
                                                                            @Parameter(description = "Number of flight records to be returned per page", example = "10", required = true)
                                                                            int size,
                                                                            @RequestParam @Min(0) @NotNull
                                                                            @Parameter(description = "Zero-based page number to be returned", example = "0", required = true)
                                                                            int page) {
        Page<ResponseFlightDto> flights = flightService.getAllFlightsByOriginDestinationAndDate(origin, destination, date, status, page, size);
        return ResponseEntity.ok(PagedResponse.from(flights));
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
        Page<ResponseFlightsGenerationDto> generations = flightService.getAllFlightGenerations(page, size);
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
        return ResponseEntity.ok(flightService.getFlightGeneration(id));
    }

}
