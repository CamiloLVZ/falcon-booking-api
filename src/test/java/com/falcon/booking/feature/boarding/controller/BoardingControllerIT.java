package com.falcon.booking.feature.boarding.controller;

import com.falcon.booking.common.enums.BoardingPassStatus;
import com.falcon.booking.common.enums.SeatClass;
import com.falcon.booking.feature.boarding.dto.BoardingPassValidationResponseDto;
import com.falcon.booking.feature.boarding.exception.BoardingPassNotFoundException;
import com.falcon.booking.feature.boarding.service.BoardingService;
import com.falcon.booking.security.jwt.JwtFilter;
import com.falcon.booking.security.jwt.JwtUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BoardingController.class)
@AutoConfigureMockMvc(addFilters = false)
class BoardingControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private JwtFilter jwtFilter;

    @MockitoBean
    private BoardingService boardingService;

    @DisplayName("Should return 200 OK and boarding pass details on validation")
    @Test
    void shouldReturn200_validateBoardingPass() throws Exception {
        UUID qrToken = UUID.randomUUID();
        BoardingPassValidationResponseDto responseDto = new BoardingPassValidationResponseDto(
                qrToken, "ANA PEREZ", "12345", "AV1234",
                "BOG", "MDE", OffsetDateTime.now().plusHours(2),
                SeatClass.ECONOMY, 12, "3B", BoardingPassStatus.ISSUED);

        given(boardingService.validate(qrToken)).willReturn(responseDto);

        ResultActions response = mockMvc.perform(get("/v1/boarding-passes/{qrToken}", qrToken));

        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.qrToken").value(qrToken.toString()))
                .andExpect(jsonPath("$.passengerName").value("ANA PEREZ"))
                .andExpect(jsonPath("$.flightNumber").value("AV1234"))
                .andExpect(jsonPath("$.seatClass").value("ECONOMY"))
                .andExpect(jsonPath("$.seatLabel").value("3B"))
                .andExpect(jsonPath("$.status").value("ISSUED"));
    }

    @DisplayName("Should return 404 when boarding pass is not found")
    @Test
    void shouldReturn404_validateBoardingPass() throws Exception {
        UUID qrToken = UUID.randomUUID();
        given(boardingService.validate(qrToken)).willThrow(new BoardingPassNotFoundException(qrToken));

        ResultActions response = mockMvc.perform(get("/v1/boarding-passes/{qrToken}", qrToken));

        response.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.type").value("boarding-pass-not-found"));
    }

    @DisplayName("Should return 200 OK when passenger is boarded via QR")
    @Test
    void shouldReturn200_boardPassengerViaQr() throws Exception {
        UUID qrToken = UUID.randomUUID();

        ResultActions response = mockMvc.perform(patch("/v1/boarding-passes/board/{qrToken}", qrToken));

        response.andExpect(status().isOk());
    }

    @DisplayName("Should return 200 and PDF when downloading boarding pass")
    @Test
    void shouldReturn200_downloadBoardingPass() throws Exception {
        Long passengerReservationId = 10L;
        byte[] pdfBytes = {1, 2, 3, 4, 5};

        given(boardingService.generatePdf(passengerReservationId)).willReturn(pdfBytes);

        ResultActions response = mockMvc.perform(get("/v1/boarding-passes/{passengerReservationId}/download", passengerReservationId));

        response.andExpect(status().isOk())
                .andExpect(content().contentType("application/pdf"))
                .andExpect(content().bytes(pdfBytes));
    }

    @DisplayName("Should return 404 when downloading boarding pass for non-existent reservation")
    @Test
    void shouldReturn404_downloadBoardingPass() throws Exception {
        Long passengerReservationId = 99L;

        given(boardingService.generatePdf(passengerReservationId))
                .willThrow(new com.falcon.booking.feature.reservation.exception.PassengerReservationNotFoundException(passengerReservationId));

        ResultActions response = mockMvc.perform(get("/v1/boarding-passes/{passengerReservationId}/download", passengerReservationId));

        response.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.type").value("passenger-reservation-does-not-exist"));
    }
}
