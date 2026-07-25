package com.falcon.booking.feature.payment.controller;

import com.falcon.booking.common.web.Error;
import com.falcon.booking.feature.auth.service.UserService;
import com.falcon.booking.feature.payment.dto.FlightPriceQuoteDto;
import com.falcon.booking.feature.payment.dto.PaymentRequestDto;
import com.falcon.booking.feature.payment.dto.ResponsePaymentDto;
import com.falcon.booking.feature.payment.service.PaymentService;
import com.falcon.booking.feature.payment.service.PricingService;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1")
@Tag(name = "Payment", description = "Operations related to flight pricing and payments")
@Validated
public class PaymentController {

    private final PricingService pricingService;
    private final PaymentService paymentService;
    private final UserService userService;

    public PaymentController(PricingService pricingService, PaymentService paymentService, UserService userService) {
        this.pricingService = pricingService;
        this.paymentService = paymentService;
        this.userService = userService;
    }

    @Operation(summary = "Get flight price quote",
            description = "Retrieves the price quote for a specific flight including the base fare and applicable taxes. This endpoint does not require authentication",
            security = {})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Flight price quote retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = FlightPriceQuoteDto.class))),
            @ApiResponse(responseCode = "400", description = "Error by invalid flight identifier",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "404", description = "Flight not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class)))
    })
    @GetMapping("/flights/{id}/quote")
    public ResponseEntity<FlightPriceQuoteDto> getFlightQuote(
            @PathVariable
            @Parameter(description = "Flight numeric unique identifier", example = "100")
            Long id) {
        return ResponseEntity.ok(pricingService.getQuote(id));
    }

    @Operation(summary = "Process payment and create reservation",
            description = "Processes a payment for one or more flights and creates a reservation with associated passengers. If authenticated as a CLIENT, the reservation is automatically linked to the account.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Payment processed successfully and reservation created",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponsePaymentDto.class))),
            @ApiResponse(responseCode = "400", description = "Error by invalid request body or payment validation failure",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "404", description = "Flight or passenger not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "409", description = "Insufficient available seats or payment conflict",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class)))
    })
    @PostMapping("/payments")
    public ResponseEntity<ResponsePaymentDto> processPayment(
            @Valid
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Payment request containing flight selections and passenger information",
                    required = true)
            @RequestBody PaymentRequestDto requestDto,
            Authentication authentication) {
        UserEntity user = resolveUserOrNull(authentication);
        return ResponseEntity.status(HttpStatus.CREATED).body(paymentService.processPayment(requestDto, user));
    }

    private UserEntity resolveUserOrNull(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        if (!(authentication.getPrincipal() instanceof JwtPayload payload)) {
            return null;
        }
        return userService.getUserById(payload.userId());
    }
}
