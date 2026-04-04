package com.falcon.booking.web.controller;

import com.falcon.booking.domain.service.AirportService;
import com.falcon.booking.web.dto.AirportDto;
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
import jakarta.validation.constraints.Size;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Airports", description = "Airport retrieval operations")
@RestController
@RequestMapping("/v1/airports")
@Validated
public class AirportController {

    private final AirportService airportService;

    @Autowired
    public AirportController(AirportService airportService) {
        this.airportService = airportService;
    }


    @Operation(summary = "Get a record by its IATA code", description = "Returns an airport record searching by its unique three character IATA code. Requires authentication with JWT token and ADMIN role",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Airport record retrieved successfully"
                    , content = @Content(mediaType = "application/json", schema = @Schema(implementation = AirportDto.class))),
            @ApiResponse(responseCode = "400", description = "Error by invalid IATA code"
                    , content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token"
                    , content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions to retrieve airports"
                    , content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "404", description = "Airport record not found"
                    , content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class))),
    })
    @GetMapping("/{iataCode}")
    public ResponseEntity<AirportDto> getAirport(@PathVariable @Size(min = 3, max = 3, message = "Iata Code must be a String with 3 characters")
                                                 @Parameter(description = "Airport unique three character IATA code", example = "BOG")
                                                 String iataCode) {
        return ResponseEntity.ok(airportService.getAirportByIataCode(iataCode));
    }


    @Operation(summary = "Get all airports", description = "Returns a list with all registered airports. Requires authentication with JWT token and ADMIN role",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Airports list retrieved successfully, even if it is empty.",
                    content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = AirportDto.class)))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions to retrieve airports",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class))),
    })
    @GetMapping
    public ResponseEntity<List<AirportDto>> getAirports() {
        return ResponseEntity.ok(airportService.getAllAirports());
    }


}
