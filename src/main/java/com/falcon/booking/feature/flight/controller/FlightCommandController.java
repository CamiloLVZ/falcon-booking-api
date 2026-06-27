package com.falcon.booking.feature.flight.controller;

import com.falcon.booking.feature.flight.service.FlightCommandService;
import com.falcon.booking.feature.flight.dto.CreateFlightDto;
import com.falcon.booking.feature.flight.dto.ResponseFlightDto;
import com.falcon.booking.common.web.Error;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Future;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Tag(name = "Flight Commands", description = "Operations related to flight state modification")
@RestController
@RequestMapping("/v1/flights")
@Validated
public class FlightCommandController {

    private final FlightCommandService flightCommandService;

    @Autowired
    public FlightCommandController(FlightCommandService flightCommandService) {
        this.flightCommandService = flightCommandService;
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
        return ResponseEntity.status(HttpStatus.CREATED).body(flightCommandService.rescheduleFLight(id, newDepartureLocalDateTime));
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
        return ResponseEntity.status(HttpStatus.CREATED).body(flightCommandService.addFlight(createFlightDto));
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
        return ResponseEntity.ok(flightCommandService.cancelFlight(id));
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
        return ResponseEntity.ok(flightCommandService.changeAirplaneType(id, idAirplaneType));
    }
}
