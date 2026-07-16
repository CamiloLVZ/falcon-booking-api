package com.falcon.booking.feature.reservation.controller;

import com.falcon.booking.common.enums.SeatClass;
import com.falcon.booking.common.web.Error;
import com.falcon.booking.feature.reservation.dto.ResponsePassengerReservationDto;
import com.falcon.booking.feature.reservation.service.CheckInService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Check-In", description = "Operations related to passenger check-in")
@RestController
@RequestMapping("/v1/reservations")
@Validated
public class CheckInController {

    private final CheckInService checkInService;

    @Autowired
    public CheckInController(CheckInService checkInService) {
        this.checkInService = checkInService;
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
                                                                            String countryIsoCode,
                                                                            @RequestParam(required = false)
                                                                            @Parameter(description = "Seat number requested. If not provided, a random one is assigned.", example = "12")
                                                                            Integer seatNumber){
        return ResponseEntity.ok(checkInService.checkInByIdentificationNumber(reservationNumber, identificationNumber, countryIsoCode, seatNumber));
    }

    @Operation(summary = "Get available seats for a flight",
            description = "Returns a list of available seat numbers for a specific flight and seat class.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of available seats retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Flight not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class)))
    })
    @GetMapping("/flight/{flightId}/available-seats")
    public ResponseEntity<List<Integer>> getAvailableSeats(@PathVariable
                                                           @Parameter(description = "Flight ID", example = "1")
                                                           Long flightId,
                                                           @RequestParam
                                                           @Parameter(description = "Seat class", example = "ECONOMY")
                                                           SeatClass seatClass) {
        return ResponseEntity.ok(checkInService.getAvailableSeats(flightId, seatClass));
    }
}
