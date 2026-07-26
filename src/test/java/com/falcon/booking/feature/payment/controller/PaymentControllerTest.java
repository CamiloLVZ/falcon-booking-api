package com.falcon.booking.feature.payment.controller;

import com.falcon.booking.common.enums.PassengerGender;
import com.falcon.booking.common.enums.PaymentStatus;
import com.falcon.booking.common.enums.SeatClass;
import com.falcon.booking.feature.auth.service.UserService;
import com.falcon.booking.feature.passenger.dto.AddPassengerDto;
import com.falcon.booking.feature.payment.dto.FlightPriceQuoteDto;
import com.falcon.booking.feature.payment.dto.PaymentPassengerDto;
import com.falcon.booking.feature.payment.dto.PaymentRequestDto;
import com.falcon.booking.feature.payment.dto.ResponsePaymentDto;
import com.falcon.booking.feature.payment.service.PaymentService;
import com.falcon.booking.feature.payment.service.PricingService;
import com.falcon.booking.security.jwt.JwtFilter;
import com.falcon.booking.security.jwt.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PaymentController.class)
@AutoConfigureMockMvc(addFilters = false)
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private JwtFilter jwtFilter;

    @MockitoBean
    private PricingService pricingService;

    @MockitoBean
    private PaymentService paymentService;

    @MockitoBean
    private UserService userService;

    private ResponsePaymentDto responsePaymentDto;

    @BeforeEach
    void setUp() {
        responsePaymentDto = new ResponsePaymentDto(
                "RES-001", BigDecimal.valueOf(250.00), PaymentStatus.APPROVED, Instant.now()
        );
    }

    @DisplayName("Should return flight price quote")
    @Test
    void shouldReturn200_getFlightQuote() throws Exception {
        FlightPriceQuoteDto quoteDto = new FlightPriceQuoteDto(1L, BigDecimal.valueOf(100), BigDecimal.valueOf(200));
        given(pricingService.getQuote(1L)).willReturn(quoteDto);

        mockMvc.perform(get("/v1/flights/{id}/quote", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.flightId").value(1))
                .andExpect(jsonPath("$.priceEconomy").value(100))
                .andExpect(jsonPath("$.priceFirstClass").value(200));
    }

    @DisplayName("Should return 201 when payment is processed successfully")
    @Test
    void shouldReturn201_processPayment() throws Exception {
        AddPassengerDto passenger = new AddPassengerDto(
                "John", "Doe", PassengerGender.M, "CO",
                LocalDate.of(1995, 1, 1), null, "123456789"
        );
        PaymentPassengerDto paymentPassenger = new PaymentPassengerDto(passenger, SeatClass.ECONOMY);
        PaymentRequestDto request = new PaymentRequestDto(1L, "contact@test.com", List.of(paymentPassenger));

        given(paymentService.processPayment(any(PaymentRequestDto.class), any())).willReturn(responsePaymentDto);

        mockMvc.perform(post("/v1/payments")
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.reservationNumber").value("RES-001"))
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }
}
