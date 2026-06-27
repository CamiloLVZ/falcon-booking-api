package com.falcon.booking.feature.reservation.controller;

import com.falcon.booking.common.web.Error;
import com.falcon.booking.feature.reservation.dto.ResponsePassengerReservationDto;
import com.falcon.booking.feature.reservation.service.BoardingService;
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

@Tag(name = "Boarding", description = "Operations related to passenger boarding")
@RestController
@RequestMapping("/v1/reservations")
@Validated
public class BoardingController {

    private final BoardingService boardingService;

    @Autowired
    public BoardingController(BoardingService boardingService) {
        this.boardingService = boardingService;
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
        return ResponseEntity.ok(boardingService.boardByIdentificationNumber(reservationNumber, identificationNumber, countryIsoCode));
    }
}
