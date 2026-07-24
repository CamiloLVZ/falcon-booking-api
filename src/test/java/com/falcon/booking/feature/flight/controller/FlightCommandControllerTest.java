package com.falcon.booking.feature.flight.controller;

import com.falcon.booking.common.enums.FlightStatus;
import com.falcon.booking.feature.airplaneType.dto.AirplaneTypeInFlightDto;
import com.falcon.booking.feature.flight.dto.CreateFlightDto;
import com.falcon.booking.feature.flight.dto.ResponseFlightDto;
import com.falcon.booking.feature.flight.service.FlightCommandService;
import com.falcon.booking.security.jwt.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WithMockUser(roles = "ADMIN")
@WebMvcTest(FlightCommandController.class)
class FlightCommandControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private FlightCommandService flightCommandService;

    @Autowired
    private ObjectMapper objectMapper;

    private ResponseFlightDto createResponseDto(Long id, String flightNumber, FlightStatus status) {
        return new ResponseFlightDto(
                id,
                flightNumber,
                "BOG",
                "MDE",
                OffsetDateTime.parse("2026-01-01T13:00:00Z"),
                LocalDateTime.parse("2026-01-01T08:00:00"),
                40,
                new AirplaneTypeInFlightDto("Airbus", "A320", 100, 10, "ABCDEF"),
                status,
                BigDecimal.valueOf(100.0),
                BigDecimal.valueOf(200.0)
        );
    }

    @DisplayName("Should return 201 Created when flight is added")
    @Test
    void shouldReturn201_addFlight() throws Exception {
        CreateFlightDto createDto = new CreateFlightDto("AV1234", LocalDateTime.now().plusDays(10));
        ResponseFlightDto responseDto = createResponseDto(1L, "AV1234", FlightStatus.SCHEDULED);
        given(flightCommandService.addFlight(createDto)).willReturn(responseDto);

        ResultActions response = mockMvc.perform(
                post("/v1/flights")
                       .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto))
                        .accept(MediaType.APPLICATION_JSON)
        );

        response.andExpect(status().isCreated())
                .andExpect(jsonPath("$.flightNumber").value("AV1234"));
    }

    @DisplayName("Should return 400 when add flight payload is invalid")
    @Test
    void shouldReturn400_addFlight() throws Exception {
        CreateFlightDto invalidDto = new CreateFlightDto("", null);

        ResultActions response = mockMvc.perform(
                post("/v1/flights")
                       .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto))
                        .accept(MediaType.APPLICATION_JSON)
        );

        response.andExpect(status().isBadRequest());
    }

    @DisplayName("Should return 201 Created when flight is rescheduled")
    @Test
    void shouldReturn201_rescheduleFlight() throws Exception {
        LocalDateTime newDeparture = LocalDateTime.now().plusDays(2);
        ResponseFlightDto responseDto = createResponseDto(10L, "AV1234", FlightStatus.SCHEDULED);
        given(flightCommandService.rescheduleFLight(10L, newDeparture)).willReturn(responseDto);

        ResultActions response = mockMvc.perform(
                post("/v1/flights/10/reschedule")
                       .with(csrf())
                        .param("newDepartureLocalDateTime", newDeparture.toString())
                        .accept(MediaType.APPLICATION_JSON)
        );

        response.andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10L));
    }

    @DisplayName("Should return 200 OK when flight is canceled")
    @Test
    void shouldReturn200_cancelFlight() throws Exception {
        ResponseFlightDto responseDto = createResponseDto(1L, "AV1234", FlightStatus.CANCELED);
        given(flightCommandService.cancelFlight(1L)).willReturn(responseDto);

        ResultActions response = mockMvc.perform(
                patch("/v1/flights/1/cancel")
                       .with(csrf())
                        .accept(MediaType.APPLICATION_JSON)
        );

        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELED"));
    }

    @DisplayName("Should return 200 OK when airplane type is changed")
    @Test
    void shouldReturn200_changeAirplaneType() throws Exception {
        ResponseFlightDto responseDto = createResponseDto(1L, "AV1234", FlightStatus.SCHEDULED);
        given(flightCommandService.changeAirplaneType(1L, 5L)).willReturn(responseDto);

        ResultActions response = mockMvc.perform(
                patch("/v1/flights/1/change-airplane-type")
                       .with(csrf())
                        .param("idAirplaneType", "5")
                        .accept(MediaType.APPLICATION_JSON)
        );

        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.flightNumber").value("AV1234"));
    }

    @DisplayName("Should return 400 when idAirplaneType is missing")
    @Test
    void shouldReturn400_changeAirplaneType() throws Exception {
        ResultActions response = mockMvc.perform(
                patch("/v1/flights/1/change-airplane-type")
                       .with(csrf())
                        .accept(MediaType.APPLICATION_JSON)
        );

        response.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").value("required-parameter-not-found"));
    }

    @DisplayName("Should return 400 when reschedule date is in the past")
    @Test
    void shouldReturn400_rescheduleFlight() throws Exception {
        LocalDateTime pastDate = LocalDateTime.now().minusDays(1);

        ResultActions response = mockMvc.perform(
                post("/v1/flights/1/reschedule")
                       .with(csrf())
                        .param("newDepartureLocalDateTime", pastDate.toString())
                        .accept(MediaType.APPLICATION_JSON)
        );

        response.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").value("invalid-arguments"));
    }
}
