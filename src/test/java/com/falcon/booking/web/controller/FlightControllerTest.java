package com.falcon.booking.web.controller;

import com.falcon.booking.domain.exception.Flight.FlightNotFoundException;
import com.falcon.booking.domain.exception.FlightGeneration.FlightGenerationNotFoundException;
import com.falcon.booking.domain.service.FlightService;
import com.falcon.booking.domain.valueobject.FlightGenerationStatus;
import com.falcon.booking.domain.valueobject.FlightGenerationType;
import com.falcon.booking.domain.valueobject.FlightStatus;
import com.falcon.booking.web.dto.airplaneType.AirplaneTypeInFlightDto;
import com.falcon.booking.web.dto.flight.CreateFlightDto;
import com.falcon.booking.web.dto.flight.ResponseFlightDto;
import com.falcon.booking.web.dto.flight.ResponseFlightsGenerationDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.falcon.booking.security.jwt.JwtUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@WithMockUser(roles = "ADMIN")
@WebMvcTest(FlightController.class)
class FlightControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private FlightService flightService;

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
                new AirplaneTypeInFlightDto("Airbus", "A320", 100, 10),
                status
        );
    }

    private ResponseFlightsGenerationDto createResponseFlightGenerationDto(Long id, FlightGenerationStatus status) {
        return new ResponseFlightsGenerationDto(
                id, status, FlightGenerationType.ROUTE, 1L, 400,
                Instant.now(), Instant.now().plusMillis(1500), 10L, "/v1/flight-generations/"+id
        );
    }

    @DisplayName("Should return 200 OK and flight by id")
    @Test
    void shouldReturn200_getFlightById() throws Exception {
        ResponseFlightDto responseDto = createResponseDto(1L, "AV1234", FlightStatus.SCHEDULED);
        given(flightService.getFlightById(1L)).willReturn(responseDto);

        ResultActions response = mockMvc.perform(get("/v1/flights/1").accept(MediaType.APPLICATION_JSON));

        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.flightNumber").value("AV1234"));
    }

    @DisplayName("Should return 404 when flight does not exist")
    @Test
    void shouldReturn400_getFlightById() throws Exception {
        given(flightService.getFlightById(1L)).willThrow(new FlightNotFoundException(1L));

        ResultActions response = mockMvc.perform(get("/v1/flights/1").accept(MediaType.APPLICATION_JSON));

        response.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.type").value("flight-does-not-exist"));
    }

    @DisplayName("Should return 200 OK and flight list")
    @Test
    void shouldReturn200_getAllFlights() throws Exception {
        List<ResponseFlightDto> flights = List.of(
                createResponseDto(1L, "AV1234", FlightStatus.SCHEDULED),
                createResponseDto(2L, "AV1234", FlightStatus.CHECK_IN_AVAILABLE)
        );

        given(flightService.getAllFlights("AV1234", FlightStatus.SCHEDULED,
                LocalDate.parse("2026-01-01"), LocalDate.parse("2026-01-31"))).willReturn(flights);

        ResultActions response = mockMvc.perform(
                get("/v1/flights")
                        .param("flightNumber", "AV1234")
                        .param("status", "SCHEDULED")
                        .param("dateFrom", "2026-01-01")
                        .param("dateTo", "2026-01-31")
                        .accept(MediaType.APPLICATION_JSON)
        );

        response.andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.size()").value(2));
    }

    @DisplayName("Should return 400 when getAllFlights has invalid flight number")
    @Test
    void shouldReturn400_getAllFlights() throws Exception {
        ResultActions response = mockMvc.perform(
                get("/v1/flights")
                        .param("flightNumber", "AV1")
                        .accept(MediaType.APPLICATION_JSON)
        );

        response.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").value("invalid-arguments"));
    }

    @DisplayName("Should return 201 Created when flight is added")
    @Test
    void shouldReturn201_addFlight() throws Exception {
        CreateFlightDto createDto = new CreateFlightDto("AV1234", LocalDateTime.now().plusDays(10));
        ResponseFlightDto responseDto = createResponseDto(1L, "AV1234", FlightStatus.SCHEDULED);
        given(flightService.addFlight(createDto)).willReturn(responseDto);

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
        given(flightService.rescheduleFLight(10L, newDeparture)).willReturn(responseDto);

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
        given(flightService.cancelFlight(1L)).willReturn(responseDto);

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
        given(flightService.changeAirplaneType(1L, 5L)).willReturn(responseDto);

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

    @DisplayName("Should return 200 OK and flight generation by id")
    @Test
    void shouldReturn200_getFlightGenerationById() throws Exception {
        ResponseFlightsGenerationDto responseDto = createResponseFlightGenerationDto(1L, FlightGenerationStatus.RUNNING);
        given(flightService.getFlightGeneration(1L)).willReturn(responseDto);

        ResultActions response = mockMvc.perform(get("/v1/flights/generations/1").accept(MediaType.APPLICATION_JSON));

        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.generationId").value(1L))
                .andExpect(jsonPath("$.status").value("RUNNING"));
    }

    @DisplayName("Should return 404 when flight does not exist")
    @Test
    void shouldReturn400_getFlightGeneration() throws Exception {
        given(flightService.getFlightGeneration(1L)).willThrow(new FlightGenerationNotFoundException(1L));

        ResultActions response = mockMvc.perform(get("/v1/flights/generations/1").accept(MediaType.APPLICATION_JSON));

        response.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.type").value("flight-generation-does-not-exist"));
    }

}







