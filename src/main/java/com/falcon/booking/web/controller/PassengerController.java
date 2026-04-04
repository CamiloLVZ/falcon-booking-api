package com.falcon.booking.web.controller;

import com.falcon.booking.domain.service.PassengerService;
import com.falcon.booking.domain.service.ReservationService;
import com.falcon.booking.web.dto.passenger.AddPassengerDto;
import com.falcon.booking.web.dto.passenger.ResponsePassengerDto;
import com.falcon.booking.web.dto.reservation.ResponseReservationDto;
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
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Passengers", description = "Operations related to passengers")
@RestController
@RequestMapping("/v1/passengers")
@Validated
public class PassengerController {

    private final PassengerService passengerService;
    private final ReservationService reservationService;

    @Autowired
    public PassengerController(PassengerService passengerService, ReservationService reservationService) {
        this.passengerService = passengerService;
        this.reservationService = reservationService;
    }

    @Operation(summary = "Get a passenger by id",
            description = "Returns a passenger record using its numeric unique identifier. Requires authentication with JWT token and ADMIN role",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Passenger retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponsePassengerDto.class))),
            @ApiResponse(responseCode = "400", description = "Error by invalid id argument",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions to retrieve passengers",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "404", description = "Passenger not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<ResponsePassengerDto> getPassengerById(@PathVariable @Parameter(description = "Passenger numeric unique identifier", example = "25")
                                                                 Long id) {
        return ResponseEntity.ok(passengerService.getPassengerById(id));
    }

    @Operation(summary = "Get a passenger by identification number",
            description = "Returns a passenger record by identification number and issuing country ISO code. Requires authentication with JWT token and ADMIN role",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Passenger retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponsePassengerDto.class))),
            @ApiResponse(responseCode = "400", description = "Error by invalid query parameters",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions to retrieve passengers",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "404", description = "Passenger not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class)))
    })
    @GetMapping("/identification")
    public ResponseEntity<ResponsePassengerDto> getPassengerByPassportNumber(@RequestParam @NotBlank
                                                                             @Parameter(description = "Passenger identification number", example = "1032456789")
                                                                             String identificationNumber,
                                                                             @RequestParam @NotBlank @Size(min = 2, max = 2)
                                                                             @Parameter(description = "Country two character ISO code", example = "CO")
                                                                             String countryIsoCode) {
        return ResponseEntity.ok(passengerService.getPassengerByIdentificationNumber(identificationNumber, countryIsoCode));
    }

    @Operation(summary = "Get a passenger by passport number",
            description = "Returns a passenger record by passport number. Requires authentication with JWT token and ADMIN role",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Passenger retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponsePassengerDto.class))),
            @ApiResponse(responseCode = "400", description = "Error by invalid passport number format",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions to retrieve passengers",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "404", description = "Passenger not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class)))
    })
    @GetMapping("/passport/{passportNumber}")
    public ResponseEntity<ResponsePassengerDto> getPassengerByPassportNumber(@PathVariable("passportNumber")
                                                                             @Parameter(description = "Passenger passport number", example = "A1234567")
                                                                             String passportNumber) {
        return ResponseEntity.ok(passengerService.getPassengerByPassportNumber(passportNumber));
    }

    @Operation(summary = "Get all reservations by passenger",
            description = "Returns all reservations linked to a passenger by identification number and country ISO code. Requires authentication with JWT token and ADMIN role",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reservation list retrieved successfully, even if list is empty",
                    content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = ResponseReservationDto.class)))),
            @ApiResponse(responseCode = "400", description = "Error by invalid query parameters",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions to retrieve passenger reservations",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "404", description = "Passenger not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class)))
    })
    @GetMapping("/reservations")
    public ResponseEntity<List<ResponseReservationDto>> getAllReservationByPassenger(@RequestParam @NotBlank
                                                                                     @Parameter(description = "Passenger identification number", example = "1032456789")
                                                                                     String identificationNumber,
                                                                                     @RequestParam @NotBlank @Size(min = 2, max = 2)
                                                                                     @Parameter(description = "Country two character ISO code", example = "CO")
                                                                                     String countryIsoCode) {
        return ResponseEntity.ok(reservationService.getAllReservationsByPassengerIdentificationNumber(identificationNumber, countryIsoCode));
    }

    @Operation(summary = "Create a passenger",
            description = "Creates a passenger record and returns the created data. Requires authentication with JWT token and ADMIN role",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Passenger created successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponsePassengerDto.class))),
            @ApiResponse(responseCode = "400", description = "Error by invalid request body",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions to create passengers",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class)))
    })
    @PostMapping
    public ResponseEntity<ResponsePassengerDto> addPassenger(@RequestBody @Valid
                                                             @io.swagger.v3.oas.annotations.parameters.RequestBody(
                                                                     description = "Data for creating a passenger",
                                                                     required = true)
                                                             AddPassengerDto addPassengerDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(passengerService.addPassenger(addPassengerDto));
    }

    @Operation(summary = "Update passenger passport number",
            description = "Updates passenger passport number using identification number and country ISO code. Requires authentication with JWT token and ADMIN role",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Passenger passport updated successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponsePassengerDto.class))),
            @ApiResponse(responseCode = "400", description = "Error by invalid query parameters",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions to update passengers",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "404", description = "Passenger not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class)))
    })
    @PatchMapping("/passport")
    public ResponseEntity<ResponsePassengerDto> patchPassengerPassportNumber(@RequestParam @NotBlank
                                                                             @Parameter(description = "Passenger identification number", example = "1032456789")
                                                                             String identificationNumber,
                                                                             @RequestParam @NotBlank @Size(min = 2, max = 2)
                                                                             @Parameter(description = "Country two character ISO code", example = "CO")
                                                                             String countryIsoCode,
                                                                             @RequestParam @NotBlank
                                                                             @Parameter(description = "New passport number", example = "B9988776")
                                                                             String newPassportNumber) {
        return ResponseEntity.ok(passengerService.updatePassengerPassport(identificationNumber, countryIsoCode, newPassportNumber));
    }

}