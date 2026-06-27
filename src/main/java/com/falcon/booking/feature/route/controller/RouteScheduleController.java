package com.falcon.booking.feature.route.controller;

import com.falcon.booking.common.web.Error;
import com.falcon.booking.feature.route.dto.AddRouteScheduleRequestDto;
import com.falcon.booking.feature.route.dto.RouteWithSchedulesDto;
import com.falcon.booking.feature.route.service.RouteSchedulesService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Size;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Route Schedules", description = "Operations related to route schedules management")
@Validated
@RestController
@RequestMapping("/v1/routes")
public class RouteScheduleController {

    private final RouteSchedulesService routeSchedulesService;

    @Autowired
    public RouteScheduleController(RouteSchedulesService routeSchedulesService) {
        this.routeSchedulesService = routeSchedulesService;
    }

    @Operation(summary = "Set route operating schedules",
            description = "Defines the set of route departure local times and week days used for flight generation. Requires authentication with JWT token and ADMIN role",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Route schedules configured successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = RouteWithSchedulesDto.class))),
            @ApiResponse(responseCode = "400", description = "Error by invalid schedule payload",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions to configure route schedules",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "404", description = "Route not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class)))
    })
    @PatchMapping("/{flightNumber}/schedules")
    public ResponseEntity<RouteWithSchedulesDto> setRouteOperatingSchedules(@PathVariable
                                                                            @Size(min = 5, max = 7, message = "Flight number must be an alphanumeric value with 5 to 7 characters")
                                                                            @Parameter(description = "Route unique flight number", example = "AV1234")
                                                                            String flightNumber,
                                                                            @RequestBody
                                                                            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                                                                                    description = "Data for setting route schedules and week days",
                                                                                    required = true)
                                                                            AddRouteScheduleRequestDto schedules) {
        return ResponseEntity.ok(routeSchedulesService.setRouteOperatingSchedules(flightNumber, schedules));
    }

    @Operation(summary = "Get route schedules",
            description = "Returns configured week days and local schedules for a route.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Route schedules retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = RouteWithSchedulesDto.class))),
            @ApiResponse(responseCode = "400", description = "Error by invalid flight number format",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "404", description = "Route not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class)))
    })
    @GetMapping("/{flightNumber}/schedules")
    public ResponseEntity<RouteWithSchedulesDto> getRouteSchedules(@PathVariable
                                                                   @Size(min = 5, max = 7, message = "Flight number must be an alphanumeric value with 5 to 7 characters")
                                                                   @Parameter(description = "Route unique flight number", example = "AV1234")
                                                                   String flightNumber) {
        return ResponseEntity.ok(routeSchedulesService.getRouteWithSchedules(flightNumber));
    }
}
