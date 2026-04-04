package com.falcon.booking.web.controller;

import com.falcon.booking.domain.service.FlightService;
import com.falcon.booking.domain.valueobject.FlightStatus;
import com.falcon.booking.web.dto.flight.CreateFlightDto;
import com.falcon.booking.web.dto.flight.ResponseFlightDto;
import com.falcon.booking.web.dto.flight.ResponseFlightsGenerationDto;
import com.falcon.booking.web.exception.Error;
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
import jakarta.validation.constraints.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

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
            description = "Returns a list of flights by route flight number with optional status and date range filters.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Flights retrieved successfully, even if list is empty",
                    content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = ResponseFlightDto.class)))),
            @ApiResponse(responseCode = "400", description = "Error by invalid query parameters",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class)))
    })
    @GetMapping
    public ResponseEntity<List<ResponseFlightDto>> getAllFlights(@RequestParam @NotNull
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
                                                                 LocalDate dateTo
    ) {
        return ResponseEntity.ok(flightService.getAllFlights(flightNumber, status, dateFrom, dateTo));
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


    @Operation(summary = "Get all flight generations",
            description = "Returns a list of all the historic flight generations. Requires authentication with JWT token and ADMIN role",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Flight generations retrieved successfully, even if list is empty",
                    content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = ResponseFlightsGenerationDto.class)))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions to retrieve flight generations",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class)))
    })
    @GetMapping("/generations")
    public ResponseEntity<List<ResponseFlightsGenerationDto>> getAllFlightGenerations() {
        return ResponseEntity.ok(flightService.getAllFlightGenerations());
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