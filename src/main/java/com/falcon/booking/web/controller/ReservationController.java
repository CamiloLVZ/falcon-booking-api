package com.falcon.booking.web.controller;

import com.falcon.booking.domain.service.ReservationService;
import com.falcon.booking.web.dto.reservation.AddReservationDto;
import com.falcon.booking.web.dto.reservation.ResponsePassengerReservationDto;
import com.falcon.booking.web.dto.reservation.ResponseReservationDto;
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
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Reservations", description = "Operations related to reservations and passenger reservation lifecycle")
@RestController
@RequestMapping("/v1/reservations")
@Validated
public class ReservationController {
    private final ReservationService reservationService;

    @Autowired
    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @Operation(summary = "Get a reservation by number",
            description = "Returns a reservation record using the reservation number.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reservation retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseReservationDto.class))),
            @ApiResponse(responseCode = "400", description = "Error by invalid reservation number format",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "404", description = "Reservation not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class)))
    })
    @GetMapping("/{reservationNumber}")
    public ResponseEntity<ResponseReservationDto> getReservation(@PathVariable
                                                                 @Parameter(description = "Reservation unique number", example = "ABC123")
                                                                 String reservationNumber) {
        return ResponseEntity.ok(reservationService.getReservationByNumber(reservationNumber));
    }

    @Operation(summary = "Get reservations by flight",
            description = "Returns all reservations associated with a flight id.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reservations retrieved successfully, even if list is empty",
                    content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = ResponseReservationDto.class)))),
            @ApiResponse(responseCode = "400", description = "Error by invalid flight id",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "404", description = "Flight not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class)))
    })
    @GetMapping("/flight/{flightId}")
    public ResponseEntity<List<ResponseReservationDto>> getAllReservationsByFlight(@PathVariable
                                                                                   @Parameter(description = "Flight numeric unique identifier", example = "100")
                                                                                   Long flightId) {
        return ResponseEntity.ok(reservationService.getAllReservationsByFlight(flightId));
    }

    @Operation(summary = "Cancel reservation",
            description = "Cancels a reservation and updates its status when cancellation is allowed.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reservation canceled successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseReservationDto.class))),
            @ApiResponse(responseCode = "400", description = "Error by invalid reservation state",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "404", description = "Reservation not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class)))
    })
    @PatchMapping("/{reservationNumber}/cancel")
    public ResponseEntity<ResponseReservationDto> cancelReservation(@PathVariable
                                                                    @Parameter(description = "Reservation unique number", example = "ABC123")
                                                                    String reservationNumber) {
        return ResponseEntity.ok(reservationService.cancelReservation(reservationNumber));
    }

    @Operation(summary = "Cancel passenger reservation by identification",
            description = "Cancels one passenger assignment in a reservation using identification number and country ISO code.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Passenger reservation canceled successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseReservationDto.class))),
            @ApiResponse(responseCode = "400", description = "Error by invalid query parameters or invalid reservation state",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "404", description = "Reservation or passenger not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class)))
    })
    @PatchMapping("/{reservationNumber}/cancel/passenger")
    public ResponseEntity<ResponseReservationDto> cancelPassengerReservation(@PathVariable
                                                                             @Parameter(description = "Reservation unique number", example = "ABC123")
                                                                             String reservationNumber,
                                                                             @RequestParam @NotBlank
                                                                             @Parameter(description = "Passenger identification number", example = "1032456789")
                                                                             String identificationNumber,
                                                                             @RequestParam @NotBlank @Size(min = 2, max = 2)
                                                                             @Parameter(description = "Country two character ISO code", example = "CO")
                                                                             String countryIsoCode) {
        return ResponseEntity.ok(reservationService.cancelPassengerReservationByIdentificationNumber(reservationNumber, identificationNumber, countryIsoCode));
    }

    @Operation(summary = "Cancel passenger reservation by passport",
            description = "Cancels one passenger assignment in a reservation using passenger passport number.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Passenger reservation canceled successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseReservationDto.class))),
            @ApiResponse(responseCode = "400", description = "Error by invalid passport number format",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "404", description = "Reservation or passenger not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class)))
    })
    @PatchMapping("/{reservationNumber}/cancel/passenger/{passportNumber}")
    public ResponseEntity<ResponseReservationDto> cancelPassengerReservation(@PathVariable
                                                                             @Parameter(description = "Reservation unique number", example = "ABC123")
                                                                             String reservationNumber,
                                                                             @PathVariable
                                                                             @Parameter(description = "Passenger passport number", example = "A1234567")
                                                                             String passportNumber) {
        return ResponseEntity.ok(reservationService.cancelPassengerReservationByPassportNumber(reservationNumber, passportNumber));
    }

    @Operation(summary = "Create a reservation",
            description = "Creates a reservation with one to three passenger-seat assignments for a given flight.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Reservation created successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseReservationDto.class))),
            @ApiResponse(responseCode = "400", description = "Error by invalid request body or business rules",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "404", description = "Flight not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class)))
    })
    @PostMapping
    public ResponseEntity<ResponseReservationDto> addReservation(@RequestBody @Valid
                                                                 @io.swagger.v3.oas.annotations.parameters.RequestBody(
                                                                         description = "Data for creating a reservation",
                                                                         required = true)
                                                                 AddReservationDto addReservationDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(reservationService.addReservation(addReservationDto));
    }

    @Operation(summary = "Check in passenger",
            description = "Marks a passenger reservation as checked in using passenger identification and country ISO code.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Passenger checked in successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponsePassengerReservationDto.class))),
            @ApiResponse(responseCode = "400", description = "Error by invalid query parameters or check-in window",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "404", description = "Reservation or passenger not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class)))
    })
    @PatchMapping("/{reservationNumber}/check-in")
    public ResponseEntity<ResponsePassengerReservationDto> checkInPassenger(@PathVariable
                                                                            @Parameter(description = "Reservation unique number", example = "ABC123")
                                                                            String reservationNumber,
                                                                            @RequestParam @NotBlank
                                                                            @Parameter(description = "Passenger identification number", example = "1032456789")
                                                                            String identificationNumber,
                                                                            @RequestParam @NotBlank @Size(min = 2, max = 2)
                                                                            @Parameter(description = "Country two character ISO code", example = "CO")
                                                                            String countryIsoCode){
        return ResponseEntity.ok(reservationService.checkInByIdentificationNumber(reservationNumber, identificationNumber, countryIsoCode));
    }

    @Operation(summary = "Board passenger",
            description = "Marks a passenger reservation as boarded using passenger identification and country ISO code.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Passenger boarded successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponsePassengerReservationDto.class))),
            @ApiResponse(responseCode = "400", description = "Error by invalid query parameters or boarding window",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "404", description = "Reservation or passenger not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class)))
    })
    @PatchMapping("/{reservationNumber}/board")
    public ResponseEntity<ResponsePassengerReservationDto> boardPassenger(@PathVariable
                                                                          @Parameter(description = "Reservation unique number", example = "ABC123")
                                                                          String reservationNumber,
                                                                          @RequestParam @NotBlank
                                                                          @Parameter(description = "Passenger identification number", example = "1032456789")
                                                                          String identificationNumber,
                                                                          @RequestParam @NotBlank @Size(min = 2, max = 2)
                                                                          @Parameter(description = "Country two character ISO code", example = "CO")
                                                                          String countryIsoCode){
        return ResponseEntity.ok(reservationService.boardByIdentificationNumber(reservationNumber, identificationNumber, countryIsoCode));
    }

}