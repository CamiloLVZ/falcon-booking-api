package com.falcon.booking.feature.boarding.controller;

import com.falcon.booking.common.web.Error;
import com.falcon.booking.feature.boarding.dto.BoardingPassValidationResponseDto;
import com.falcon.booking.feature.boarding.service.BoardingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Boarding", description = "Operations related to passenger boarding")
@RestController
@RequestMapping("/v1/boarding-passes")
@Validated
public class BoardingController {

    private final BoardingService boardingService;

    @Autowired
    public BoardingController(BoardingService boardingService) {
        this.boardingService = boardingService;
    }

    @Operation(summary = "Validate Boarding Pass", description = "Validates the QR token and retrieves boarding pass information")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Boarding pass is valid",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = BoardingPassValidationResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Boarding pass not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class)))
    })
    @GetMapping("/{qrToken}")
    public ResponseEntity<BoardingPassValidationResponseDto> validateBoardingPass(@PathVariable @Parameter(description = "UUID QR token") UUID qrToken) {
        BoardingPassValidationResponseDto response = boardingService.validate(qrToken);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Board Passenger via QR", description = "Marks the passenger and boarding pass as boarded via QR token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Passenger boarded successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid passenger reservation state or flight state",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "404", description = "Boarding pass not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "409", description = "Boarding pass already boarded or expired",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class)))
    })
    @PatchMapping("/board/{qrToken}")
    public ResponseEntity<Void> boardPassengerViaQr(@PathVariable @Parameter(description = "UUID QR token") UUID qrToken) {
        boardingService.boardPassenger(qrToken);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Download Boarding Pass PDF", description = "Generates and downloads the boarding pass PDF for a given passenger reservation ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "PDF generated successfully",
                    content = @Content(mediaType = "application/pdf")),
            @ApiResponse(responseCode = "404", description = "Passenger reservation not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "500", description = "PDF generation error",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class)))
    })
    @GetMapping("/{passengerReservationId}/download")
    public ResponseEntity<ByteArrayResource> downloadBoardingPass(@PathVariable @Parameter(description = "Passenger reservation ID") Long passengerReservationId) {
        byte[] pdf = boardingService.generatePdf(passengerReservationId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "boarding-pass.pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .body(new ByteArrayResource(pdf));
    }
}
