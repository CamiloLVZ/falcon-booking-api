package com.falcon.booking.feature.reservation.controller;

import com.falcon.booking.common.enums.ReservationStatus;
import com.falcon.booking.common.web.Error;
import com.falcon.booking.common.web.PagedResponse;
import com.falcon.booking.feature.auth.service.UserService;
import com.falcon.booking.feature.reservation.dto.ResponseReservationDto;
import com.falcon.booking.feature.reservation.dto.ReservationAccessRequestDto;
import com.falcon.booking.feature.reservation.exception.InvalidReservationAccessException;
import com.falcon.booking.feature.reservation.service.ReservationAccessService;
import com.falcon.booking.feature.reservation.service.ReservationCommandService;
import com.falcon.booking.feature.reservation.service.ReservationQueryService;
import com.falcon.booking.persistence.entity.UserEntity;
import com.falcon.booking.security.jwt.JwtPayload;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Reservations", description = "Operations related to reservations and passenger reservation lifecycle")
@RestController
@RequestMapping("/v1/reservations")
@Validated
public class ReservationController {
    private final ReservationQueryService reservationQueryService;
    private final ReservationCommandService reservationCommandService;
    private final ReservationAccessService reservationAccessService;
    private final UserService userService;

    @Autowired
    public ReservationController(ReservationQueryService reservationQueryService,
                                 ReservationCommandService reservationCommandService,
                                 ReservationAccessService reservationAccessService,
                                 UserService userService) {
        this.reservationQueryService = reservationQueryService;
        this.reservationCommandService = reservationCommandService;
        this.reservationAccessService = reservationAccessService;
        this.userService = userService;
    }

    @Operation(summary = "Get a reservation by number",
            description = "Returns a reservation record using the reservation number. Guests must also provide the contact email associated with the reservation.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reservation retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseReservationDto.class))),
            @ApiResponse(responseCode = "400", description = "Error by invalid reservation number format",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "404", description = "Reservation not found or contact email is invalid",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class)))
    })
    @GetMapping("/{reservationNumber}")
    public ResponseEntity<ResponseReservationDto> getReservation(@PathVariable
                                                                 @Parameter(description = "Reservation unique number", example = "ABC123")
                                                                 String reservationNumber,
                                                                 @RequestParam(required = false)
                                                                 @Parameter(description = "Required for guests: contact email associated with the reservation", example = "contact@example.com")
                                                                 String contactEmail,
                                                                 Authentication authentication) {
        if (!isAuthenticated(authentication)) {
            if (contactEmail == null || contactEmail.isBlank()) {
                throw new InvalidReservationAccessException();
            }
            reservationAccessService.getReservationByNumberAndContactEmail(reservationNumber, contactEmail);
        }
        return ResponseEntity.ok(reservationQueryService.getReservationByNumber(reservationNumber));
    }

    @Operation(summary = "Get reservations by flight",
            description = "Returns a paginated list of reservations associated with a flight id.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Paginated reservations retrieved successfully, even if content is empty",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = PagedResponse.class))),
            @ApiResponse(responseCode = "400", description = "Error by invalid flight id or pagination parameters",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "404", description = "Flight not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class)))
    })
    @GetMapping("/flight/{flightId}")
    public ResponseEntity<PagedResponse<ResponseReservationDto>> getAllReservationsByFlight(@PathVariable
                                                                                   @Parameter(description = "Flight numeric unique identifier", example = "100")
                                                                                   Long flightId,
                                                                                   @RequestParam @Min(1) @NotNull
                                                                                   @Parameter(description = "Number of reservation records to be returned per page", example = "10", required = true)
                                                                                   int size,
                                                                                   @RequestParam @Min(0) @NotNull
                                                                                   @Parameter(description = "Zero-based page number to be returned", example = "0", required = true)
                                                                                   int page) {
        Page<ResponseReservationDto> reservations = reservationQueryService.getAllReservationsByFlight(flightId, page, size);
        return ResponseEntity.ok(PagedResponse.from(reservations));
    }

    @Operation(summary = "Get my reservations",
            description = "Returns a paginated list of reservations belonging to the authenticated client. Optionally filter by status.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Paginated reservations retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = PagedResponse.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class)))
    })
    @GetMapping("/me")
    public ResponseEntity<PagedResponse<ResponseReservationDto>> getMyReservations(@RequestParam(required = false)
                                                                                   @Parameter(description = "Optional filter by reservation status", example = "RESERVED")
                                                                                   ReservationStatus status,
                                                                                   @RequestParam @Min(1) @NotNull
                                                                                   @Parameter(description = "Number of records per page", example = "10", required = true)
                                                                                   int size,
                                                                                   @RequestParam @Min(0) @NotNull
                                                                                   @Parameter(description = "Zero-based page number", example = "0", required = true)
                                                                                   int page,
                                                                                   Authentication authentication) {
        UserEntity user = resolveUser(authentication);
        Page<ResponseReservationDto> reservations = reservationQueryService.getMyReservations(user, status, page, size);
        return ResponseEntity.ok(PagedResponse.from(reservations));
    }

    @Operation(summary = "Get reservation with details",
            description = "Returns a reservation with full passenger details and flight information in a single response, using the reservation database ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reservation details retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseReservationDto.class))),
            @ApiResponse(responseCode = "404", description = "Reservation not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class)))
    })
    @GetMapping("/{id}/with-details")
    public ResponseEntity<ResponseReservationDto> getReservationWithDetails(@PathVariable
                                                                            @Parameter(description = "Reservation numeric unique identifier", example = "1")
                                                                            Long id) {
        return ResponseEntity.ok(reservationQueryService.getReservationById(id));
    }

    @Operation(summary = "Cancel reservation",
            description = "Cancels a reservation after verifying its number and contact email. No authentication is required.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reservation canceled successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseReservationDto.class))),
            @ApiResponse(responseCode = "400", description = "Error by invalid reservation state or cancellation window expired",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "404", description = "Reservation number or contact email is invalid",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "404", description = "Reservation not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class)))
    })
    @PatchMapping("/{reservationNumber}/cancel")
    public ResponseEntity<ResponseReservationDto> cancelReservation(@PathVariable
                                                                    @Parameter(description = "Reservation unique number", example = "ABC123")
                                                                    String reservationNumber,
                                                                    @Valid @RequestBody ReservationAccessRequestDto request) {
        return ResponseEntity.ok(reservationCommandService.cancelReservationByContactEmail(reservationNumber, request.contactEmail()));
    }

    @Operation(summary = "Cancel passenger reservation by identification",
            description = "Cancels one passenger assignment after verifying the reservation number and contact email.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Passenger reservation canceled successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseReservationDto.class))),
            @ApiResponse(responseCode = "400", description = "Error by invalid query parameters or invalid reservation state",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "404", description = "Reservation number or contact email is invalid",
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
                                                                             String countryIsoCode,
                                                                             @Valid @RequestBody ReservationAccessRequestDto request) {
        return ResponseEntity.ok(reservationCommandService.cancelPassengerReservationByIdentificationNumber(reservationNumber, request.contactEmail(), identificationNumber, countryIsoCode));
    }

    @Operation(summary = "Cancel passenger reservation by passport",
            description = "Cancels one passenger assignment after verifying the reservation number and contact email.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Passenger reservation canceled successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseReservationDto.class))),
            @ApiResponse(responseCode = "400", description = "Error by invalid passport number format",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "404", description = "Reservation number or contact email is invalid",
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
                                                                             String passportNumber,
                                                                             @Valid @RequestBody ReservationAccessRequestDto request) {
        return ResponseEntity.ok(reservationCommandService.cancelPassengerReservationByPassportNumber(reservationNumber, request.contactEmail(), passportNumber));
    }

    private UserEntity resolveUser(Authentication authentication) {
        JwtPayload payload = (JwtPayload) authentication.getPrincipal();
        return userService.getUserById(payload.userId());
    }

    private boolean isAuthenticated(Authentication authentication) {
        return authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken);
    }

}
