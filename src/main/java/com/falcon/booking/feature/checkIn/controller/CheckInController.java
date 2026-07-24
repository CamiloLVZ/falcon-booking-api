package com.falcon.booking.feature.checkIn.controller;

import com.falcon.booking.common.web.Error;
import com.falcon.booking.feature.boarding.service.BoardingService;
import com.falcon.booking.feature.checkIn.dto.CheckInRequestDto;
import com.falcon.booking.feature.checkIn.service.CheckInService;
import com.falcon.booking.feature.reservation.dto.ResponsePassengerReservationDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Check-In", description = "Operations related to passenger check-in")
@RestController
@RequestMapping("/v1/check-in")
@Validated
public class CheckInController {

    private final CheckInService checkInService;
    private final BoardingService boardingService;

    @Autowired
    public CheckInController(CheckInService checkInService, BoardingService boardingService) {
        this.checkInService = checkInService;
        this.boardingService = boardingService;
    }

    @Operation(summary = "Check in passenger",
            description = "Marks a passenger reservation as checked in using passenger identification and country ISO code.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Passenger checked in successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponsePassengerReservationDto.class))),
            @ApiResponse(responseCode = "400", description = "Error by invalid request body or check-in window",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "404", description = "Reservation or passenger not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "409", description = "Seat number already taken",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class)))
    })
    @PostMapping
    public ResponseEntity<ResponsePassengerReservationDto> checkInPassenger(@Valid @RequestBody CheckInRequestDto request){
        ResponsePassengerReservationDto response = checkInService.checkInByIdentificationNumber(request.reservationNumber(), request.identificationNumber(), request.countryIsoCode(), request.seatNumber());
        boardingService.issue(response.id());
        return ResponseEntity.ok(response);
    }

}
