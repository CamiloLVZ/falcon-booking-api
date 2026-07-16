package com.falcon.booking.feature.payment.controller;

import com.falcon.booking.feature.payment.dto.FlightPriceQuoteDto;
import com.falcon.booking.feature.payment.dto.PaymentRequestDto;
import com.falcon.booking.feature.payment.dto.ResponsePaymentDto;
import com.falcon.booking.feature.payment.service.PaymentService;
import com.falcon.booking.feature.payment.service.PricingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1")
@Tag(name = "Payment", description = "Operations related to flight pricing and payments")
public class PaymentController {

    private final PricingService pricingService;
    private final PaymentService paymentService;

    public PaymentController(PricingService pricingService, PaymentService paymentService) {
        this.pricingService = pricingService;
        this.paymentService = paymentService;
    }

    @GetMapping("/flights/{id}/quote")
    @Operation(summary = "Get flight price quote")
    public FlightPriceQuoteDto getFlightQuote(@PathVariable Long id) {
        return pricingService.getQuote(id);
    }

    @PostMapping("/payments")
    @Operation(summary = "Process payment and create reservation")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponsePaymentDto processPayment(@Valid @RequestBody PaymentRequestDto requestDto) {
        return paymentService.processPayment(requestDto);
    }
}
