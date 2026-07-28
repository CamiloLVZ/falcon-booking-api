package com.falcon.booking.feature.route.controller;

import com.falcon.booking.common.enums.RouteStatus;
import com.falcon.booking.common.web.Error;
import com.falcon.booking.common.web.PagedResponse;
import com.falcon.booking.feature.airport.dto.AirportSearchOptionDto;
import com.falcon.booking.feature.flight.dto.ResponseFlightDto;
import com.falcon.booking.feature.flight.service.FlightQueryService;
import com.falcon.booking.feature.flightGeneration.dto.ResponseFlightsGenerationDto;
import com.falcon.booking.feature.flightGeneration.service.FlightGenerationService;
import com.falcon.booking.feature.route.dto.CreateRouteDto;
import com.falcon.booking.feature.route.dto.ResponseRouteDto;
import com.falcon.booking.feature.route.dto.UpdateRouteDto;
import com.falcon.booking.feature.route.service.RouteActivationOrchestrator;
import com.falcon.booking.feature.route.service.RouteCommandService;
import com.falcon.booking.feature.route.service.RouteQueryService;
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

import java.time.LocalDate;
import java.util.List;

@Tag(name = "Routes", description = "Operations related to route management and route flight generation")
@Validated
@RestController
@RequestMapping("/v1/routes")
public class RouteController {

    private final RouteQueryService routeQueryService;
    private final RouteCommandService routeCommandService;
    private final FlightQueryService flightQueryService;
    private final FlightGenerationService flightGenerationService;
    private final RouteActivationOrchestrator routeActivationOrchestrator;

    @Autowired
    public RouteController(RouteQueryService routeQueryService, RouteCommandService routeCommandService, FlightQueryService flightQueryService, FlightGenerationService flightGenerationService, RouteActivationOrchestrator routeActivationOrchestrator) {
        this.routeQueryService = routeQueryService;
        this.routeCommandService = routeCommandService;
        this.flightQueryService = flightQueryService;
        this.flightGenerationService = flightGenerationService;
        this.routeActivationOrchestrator = routeActivationOrchestrator;
    }

    @Operation(summary = "Get all routes",
            description = "Returns all routes with optional filters by origin IATA, destination IATA, status, flight number and airplane type.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Paginated routes retrieved successfully, even if content is empty",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = PagedResponse.class))),
            @ApiResponse(responseCode = "400", description = "Error by invalid query or pagination parameters",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class)))
    })
    @GetMapping
    public ResponseEntity<PagedResponse<ResponseRouteDto>> getAllRoutes(@RequestParam(required = false) @Size(min = 3, max = 3, message = "Iata Code must be a 3 letter String")
                                                               @Parameter(description = "Origin airport IATA code", example = "BOG")
                                                               String originAirportIataCode,
                                                               @RequestParam(required = false) @Size(min = 3, max = 3, message = "Iata Code must be a 3 letter String")
                                                               @Parameter(description = "Destination airport IATA code", example = "MIA")
                                                               String destinationAirportIataCode,
                                                               @RequestParam(required = false)
                                                               @Parameter(description = "Route status", example = "ACTIVE")
                                                               RouteStatus status,
                                                               @RequestParam(required = false)
                                                               @Parameter(description = "Filter by flight number (partial match)", example = "AV")
                                                               String flightNumber,
                                                               @RequestParam(required = false)
                                                               @Parameter(description = "Filter by default airplane type ID", example = "1")
                                                               Long airplaneTypeId,
                                                               @RequestParam @Min(1) @NotNull
                                                               @Parameter(description = "Number of route records to be returned per page", example = "10", required = true)
                                                               int size,
                                                               @RequestParam @Min(0) @NotNull
                                                               @Parameter(description = "Zero-based page number to be returned", example = "0", required = true)
                                                               int page) {

        Page<ResponseRouteDto> routes = routeQueryService.getAllRoutes(originAirportIataCode, destinationAirportIataCode, status, flightNumber, airplaneTypeId, page, size);
        return ResponseEntity.ok(PagedResponse.from(routes));

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
        return ResponseEntity.ok(routeQueryService.getRouteByFlightNumber(flightNumber));
    }

    @Operation(summary = "Get origin airports for search",
            description = "Returns a list of airports with active routes as origin options for route search filters.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Origin airport options retrieved successfully, even if list is empty",
                    content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = AirportSearchOptionDto.class))))
    })
    @GetMapping("/search/origins")
    public ResponseEntity<List<AirportSearchOptionDto>> getOriginAirports() {
        return ResponseEntity.ok(routeQueryService.getOriginAirports());
    }

    @Operation(summary = "Get destination airports for search",
            description = "Returns a list of destination airports with active routes from the selected origin airport.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Destination airport options retrieved successfully, even if list is empty",
                    content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = AirportSearchOptionDto.class)))),
            @ApiResponse(responseCode = "400", description = "Error by invalid origin IATA code",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class)))
    })
    @GetMapping("/search/destinations")
    public ResponseEntity<List<AirportSearchOptionDto>> getDestinationAirports(@RequestParam
                                                                               @NotNull(message = "originIataCode is required")
                                                                               @Size(min = 3, max = 3, message = "Iata Code must be a 3 letter String")
                                                                               @Parameter(description = "Origin airport IATA code used to find available destinations", example = "BOG")
                                                                               String originIataCode) {
        return ResponseEntity.ok(routeQueryService.getDestinationAirports(originIataCode));
    }

     @Operation(summary = "Create a route",
             description = "Creates a route record using origin and destination airports and default airplane type. Requires authentication with JWT token and ADMIN role",
             security = @SecurityRequirement(name = "bearerAuth"))
     @ApiResponses(value = {
             @ApiResponse(responseCode = "201", description = "Route created successfully",
                     content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseRouteDto.class))),
             @ApiResponse(responseCode = "400", description = "Error by invalid request body",
                     content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class))),
             @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token",
                     content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class))),
             @ApiResponse(responseCode = "403", description = "Insufficient permissions to create routes",
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
        return ResponseEntity.status(HttpStatus.CREATED).body(routeCommandService.addRoute(createRouteDto));
    }

     @Operation(summary = "Update route",
             description = "Updates editable route data for an existing route by flight number. Requires authentication with JWT token and ADMIN role",
             security = @SecurityRequirement(name = "bearerAuth"))
     @ApiResponses(value = {
             @ApiResponse(responseCode = "200", description = "Route updated successfully",
                     content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseRouteDto.class))),
             @ApiResponse(responseCode = "400", description = "Error by invalid request body or path variable",
                     content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class))),
             @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token",
                     content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class))),
             @ApiResponse(responseCode = "403", description = "Insufficient permissions to update routes",
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
        return ResponseEntity.ok(routeCommandService.updateRoute(flightNumber, updateRouteDto));
    }

     @Operation(summary = "Activate route",
             description = "Changes route status to ACTIVE. Requires authentication with JWT token and ADMIN role",
             security = @SecurityRequirement(name = "bearerAuth"))
     @ApiResponses(value = {
             @ApiResponse(responseCode = "200", description = "Route activated successfully",
                     content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseRouteDto.class))),
             @ApiResponse(responseCode = "400", description = "Error by invalid route state",
                     content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class))),
             @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token",
                     content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class))),
             @ApiResponse(responseCode = "403", description = "Insufficient permissions to activate routes",
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
             description = "Changes route status to INACTIVE. Requires authentication with JWT token and ADMIN role",
             security = @SecurityRequirement(name = "bearerAuth"))
     @ApiResponses(value = {
             @ApiResponse(responseCode = "200", description = "Route deactivated successfully",
                     content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseRouteDto.class))),
             @ApiResponse(responseCode = "400", description = "Error by invalid route state",
                     content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class))),
             @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token",
                     content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class))),
             @ApiResponse(responseCode = "403", description = "Insufficient permissions to deactivate routes",
                     content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class))),
             @ApiResponse(responseCode = "404", description = "Route not found",
                     content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class)))
     })
    @PatchMapping("/{flightNumber}/deactivate")
    public ResponseEntity<ResponseRouteDto> deactivateDto(@PathVariable
                                                          @Size(min = 5, max = 7, message = "Flight number must be an alphanumeric value with 5 to 7 characters")
                                                          @Parameter(description = "Route unique flight number", example = "AV1234")
                                                          String flightNumber) {
        return ResponseEntity.ok(routeCommandService.deactivateRoute(flightNumber));
    }




    @Operation(summary = "Get route flights in a specific date",
            description = "Returns generated flights for a route scheduled in a specific date (origin airport local date).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Paginated route flights retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = PagedResponse.class))),
            @ApiResponse(responseCode = "400", description = "Error by invalid date, flight number format or pagination parameters",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "404", description = "Route not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class)))
    })
    @GetMapping("/{flightNumber}/flights")
    public ResponseEntity<PagedResponse<ResponseFlightDto>> getAllFlightsByRouteAndDates(@Size(min = 5, max = 7, message = "Flight number must be an alphanumeric value with 5 to 7 characters")
                                                                                @NotNull @PathVariable
                                                                                @Parameter(description = "Route unique flight number", example = "AV1234")
                                                                                String flightNumber,
                                                                                @RequestParam @NotNull(message = "date is required")
                                                                                @Parameter(description = "Date for search", example = "2026-02-01")
                                                                                LocalDate date,
                                                                                @RequestParam @Min(1) @NotNull
                                                                                @Parameter(description = "Number of flight records to be returned per page", example = "10", required = true)
                                                                                int size,
                                                                                @RequestParam @Min(0) @NotNull
                                                                                @Parameter(description = "Zero-based page number to be returned", example = "0", required = true)
                                                                                int page
    ) {
        Page<ResponseFlightDto> flights = flightQueryService.getAllFlightsByRouteAndDate(flightNumber, date, page, size);
        return ResponseEntity.ok(PagedResponse.from(flights));
    }


     @Operation(summary = "Generate flights for a route",
             description = "Generates scheduled flights for one route according to configured route schedules. This method works asynchronously, there is not posible to execute multiple generation for same route at same time. Requires authentication with JWT token and ADMIN role",
             security = @SecurityRequirement(name = "bearerAuth"))
     @ApiResponses(value = {
             @ApiResponse(responseCode = "202", description = "Flight generation for route process started successfully",
                     content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseFlightsGenerationDto.class))),
             @ApiResponse(responseCode = "400", description = "Error by invalid route state for generation",
                     content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class))),
             @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token",
                     content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class))),
             @ApiResponse(responseCode = "403", description = "Insufficient permissions to generate flights",
                     content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class))),
             @ApiResponse(responseCode = "404", description = "Route not found",
                     content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class)))
     })
    @PostMapping("/{flightNumber}/generateFlights")
    public ResponseEntity<ResponseFlightsGenerationDto> generateFlightsForRoute(@PathVariable
                                                                               @Size(min = 5, max = 7, message = "Flight number must be an alphanumeric value with 5 to 7 characters")
                                                                               @Parameter(description = "Route unique flight number", example = "AV1234")
                                                                               String flightNumber) {

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(flightGenerationService.startRouteFlightGeneration(flightNumber));
    }


     @Operation(summary = "Generate flights for all routes",
             description = "Generates scheduled flights for all active routes. This method works asynchronously, there is not posible to execute multiple generation at same time. Requires authentication with JWT token and ADMIN role",
             security = @SecurityRequirement(name = "bearerAuth"))
     @ApiResponses(value = {
             @ApiResponse(responseCode = "202", description = "Global flight generation process started successfully",
                     content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = ResponseFlightsGenerationDto.class)))),
             @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token",
                     content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class))),
             @ApiResponse(responseCode = "403", description = "Insufficient permissions to generate flights",
                     content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class)))
     })
    @PostMapping("/generateFlights")
    public ResponseEntity<ResponseFlightsGenerationDto> generateFlightForAllRoutes() {
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(flightGenerationService.startGlobalFlightGeneration());
    }

}
