package com.falcon.booking.web.controller;

import com.falcon.booking.domain.service.FlightService;
import com.falcon.booking.domain.service.RouteActivationOrchestrator;
import com.falcon.booking.domain.service.RouteService;
import com.falcon.booking.domain.valueobject.RouteStatus;
import com.falcon.booking.web.dto.flight.ResponseFlightDto;
import com.falcon.booking.web.dto.flight.ResponseFlightsGenerationDto;
import com.falcon.booking.web.dto.route.*;
import com.falcon.booking.web.exception.Error;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "Routes", description = "Operations related to route management and route flight generation")
@Validated
@RestController
@RequestMapping("/v1/routes")
public class RouteController {

    private final RouteService routeService;
    private final FlightService flightService;
    private final RouteActivationOrchestrator routeActivationOrchestrator;

    @Autowired
    public RouteController(RouteService routeService, FlightService flightService, RouteActivationOrchestrator routeActivationOrchestrator) {
        this.routeService = routeService;
        this.flightService = flightService;
        this.routeActivationOrchestrator = routeActivationOrchestrator;
    }

    @Operation(summary = "Get all routes",
            description = "Returns all routes with optional filters by origin IATA, destination IATA and status.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Routes retrieved successfully, even if list is empty",
                    content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = ResponseRouteDto.class)))),
            @ApiResponse(responseCode = "400", description = "Error by invalid query parameters",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class)))
    })
    @GetMapping
    public ResponseEntity<List<ResponseRouteDto>> getAllRoutes(@RequestParam(required = false) @Size(min = 3, max = 3, message = "Iata Code must be a 3 letter String")
                                                               @Parameter(description = "Origin airport IATA code", example = "BOG")
                                                               String originAirportIataCode,
                                                               @RequestParam(required = false) @Size(min = 3, max = 3, message = "Iata Code must be a 3 letter String")
                                                               @Parameter(description = "Destination airport IATA code", example = "MIA")
                                                               String destinationAirportIataCode,
                                                               @RequestParam(required = false)
                                                               @Parameter(description = "Route status", example = "ACTIVE")
                                                               RouteStatus status) {

        return ResponseEntity.ok(routeService.getAllRoutes(originAirportIataCode, destinationAirportIataCode, status));

    }

    @Operation(summary = "Get route by flight number",
            description = "Returns a route record using its unique flight number.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Route retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseRouteDto.class))),
            @ApiResponse(responseCode = "400", description = "Error by invalid flight number format",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "404", description = "Route not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class)))
    })
    @GetMapping("/{flightNumber}")
    public ResponseEntity<ResponseRouteDto> getRouteByFlightNumber(@PathVariable
                                                                   @Size(min = 5, max = 7, message = "Flight number must be an alphanumeric value with 5 to 7 characters")
                                                                   @Parameter(description = "Route unique flight number", example = "AV1234")
                                                                   String flightNumber) {
        return ResponseEntity.ok(routeService.getRouteByFlightNumber(flightNumber));
    }

    @Operation(summary = "Create a route",
            description = "Creates a route record using origin and destination airports and default airplane type.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Route created successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseRouteDto.class))),
            @ApiResponse(responseCode = "400", description = "Error by invalid request body",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "404", description = "Airport or airplane type not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class)))
    })
    @PostMapping
    public ResponseEntity<ResponseRouteDto> addRoute(@RequestBody @Valid
                                                     @io.swagger.v3.oas.annotations.parameters.RequestBody(
                                                             description = "Data for creating a route",
                                                             required = true)
                                                     CreateRouteDto createRouteDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(routeService.addRoute(createRouteDto));
    }

    @Operation(summary = "Update route",
            description = "Updates editable route data for an existing route by flight number.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Route updated successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseRouteDto.class))),
            @ApiResponse(responseCode = "400", description = "Error by invalid request body or path variable",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "404", description = "Route not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class)))
    })
    @PutMapping("/{flightNumber}")
    public ResponseEntity<ResponseRouteDto> updateRoute(@PathVariable @Size(min = 5, max = 7, message = "Flight number must be an alphanumeric value with 5 to 7 characters")
                                                        @Parameter(description = "Route unique flight number", example = "AV1234")
                                                        String flightNumber,
                                                        @RequestBody @Valid
                                                        @io.swagger.v3.oas.annotations.parameters.RequestBody(
                                                                description = "Data for updating a route",
                                                                required = true)
                                                        UpdateRouteDto updateRouteDto) {
        return ResponseEntity.ok(routeService.updateRoute(flightNumber, updateRouteDto));
    }

    @Operation(summary = "Activate route",
            description = "Changes route status to ACTIVE.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Route activated successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseRouteDto.class))),
            @ApiResponse(responseCode = "400", description = "Error by invalid route state",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "404", description = "Route not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class)))
    })
    @PatchMapping("/{flightNumber}/activate")
    public ResponseEntity<ResponseRouteDto> activateDto(@PathVariable
                                                        @Size(min = 5, max = 7, message = "Flight number must be an alphanumeric value with 5 to 7 characters")
                                                        @Parameter(description = "Route unique flight number", example = "AV1234")
                                                        String flightNumber) {
        return ResponseEntity.ok(routeActivationOrchestrator.activateRoute(flightNumber));
    }

    @Operation(summary = "Deactivate route",
            description = "Changes route status to INACTIVE.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Route deactivated successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseRouteDto.class))),
            @ApiResponse(responseCode = "400", description = "Error by invalid route state",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "404", description = "Route not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class)))
    })
    @PatchMapping("/{flightNumber}/deactivate")
    public ResponseEntity<ResponseRouteDto> deactivateDto(@PathVariable
                                                          @Size(min = 5, max = 7, message = "Flight number must be an alphanumeric value with 5 to 7 characters")
                                                          @Parameter(description = "Route unique flight number", example = "AV1234")
                                                          String flightNumber) {
        return ResponseEntity.ok(routeService.deactivateRoute(flightNumber));
    }


    @Operation(summary = "Set route operating schedules",
            description = "Defines the set of route departure local times and week days used for flight generation.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Route schedules configured successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = RouteWithSchedulesDto.class))),
            @ApiResponse(responseCode = "400", description = "Error by invalid schedule payload",
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
        return ResponseEntity.ok(routeService.setRouteOperatingSchedules(flightNumber, schedules));
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
        return ResponseEntity.ok(routeService.getRouteWithSchedules(flightNumber));
    }


    @Operation(summary = "Generate flights for a route",
            description = "Generates scheduled flights for one route according to configured route schedules." +
                    "This method works asynchronously, there is not posible to execute multiple generation for same route at same time")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "202", description = "Flight generation for route process started successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseFlightsGenerationDto.class))),
            @ApiResponse(responseCode = "400", description = "Error by invalid route state for generation",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "404", description = "Route not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class)))
    })
    @PostMapping("/{flightNumber}/generateFlights")
    public ResponseEntity<ResponseFlightsGenerationDto> generateFlightsForRoute(@PathVariable
                                                                               @Size(min = 5, max = 7, message = "Flight number must be an alphanumeric value with 5 to 7 characters")
                                                                               @Parameter(description = "Route unique flight number", example = "AV1234")
                                                                               String flightNumber) {

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(flightService.startRouteFlightGeneration(flightNumber));
    }



    @Operation(summary = "Generate flights for all routes",
            description = "Generates scheduled flights for all active routes."+
                    "This method works asynchronously, there is not posible to execute multiple generation at same time")

    @ApiResponses(value = {
            @ApiResponse(responseCode = "202", description = "Global flight generation process started successfully",
                    content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = ResponseFlightsGenerationDto.class))))
    })
    @PostMapping("/generateFlights")
    public ResponseEntity<ResponseFlightsGenerationDto> generateFlightForAllRoutes() {
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(flightService.startGlobalFlightGeneration());
    }

    @Operation(summary = "Get route flights in date range",
            description = "Returns generated flights for a route between dateFrom and dateTo inclusive.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Route flights retrieved successfully",
                    content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = ResponseFlightDto.class)))),
            @ApiResponse(responseCode = "400", description = "Error by invalid date range or flight number format",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "404", description = "Route not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class)))
    })
    @GetMapping("/{flightNumber}/flights")
    public ResponseEntity<List<ResponseFlightDto>> getAllFlightsByRouteAndDates(@Size(min = 5, max = 7, message = "Flight number must be an alphanumeric value with 5 to 7 characters")
                                                                                @NotNull @PathVariable
                                                                                @Parameter(description = "Route unique flight number", example = "AV1234")
                                                                                String flightNumber,
                                                                                @RequestParam @NotNull(message = "dateFrom is required")
                                                                                @Parameter(description = "Initial date for search", example = "2026-02-01")
                                                                                LocalDate dateFrom,
                                                                                @RequestParam @NotNull(message = "dateTo is required")
                                                                                @Parameter(description = "Final date for search", example = "2026-02-28")
                                                                                LocalDate dateTo
    ) {
        return ResponseEntity.ok(flightService.getAllFlightsByRouteAndDates(flightNumber, dateFrom, dateTo));
    }

}