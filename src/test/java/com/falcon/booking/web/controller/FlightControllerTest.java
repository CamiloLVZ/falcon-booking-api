package com.falcon.booking.web.controller;

import com.falcon.booking.domain.exception.Flight.FlightNotFoundException;
import com.falcon.booking.domain.service.FlightService;
import com.falcon.booking.domain.valueobject.FlightStatus;
import com.falcon.booking.web.dto.airplaneType.AirplaneTypeInFlightDto;
import com.falcon.booking.web.dto.flight.CreateFlightDto;
import com.falcon.booking.web.dto.flight.ResponseFlightDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

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

@WebMvcTest(FlightController.class)
class FlightControllerTest {

    @Autowired
    private MockMvc mockMvc;

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

    @DisplayName("Should return 200 OK and flight by id")
    @Test
    void shouldReturn200_getFlightById() throws Exception {
        ResponseFlightDto responseDto = createResponseDto(1L, "AV1234", FlightStatus.SCHEDULED);
        given(flightService.getFlightById(1L)).willReturn(responseDto);

        ResultActions response = mockMvc.perform(get("/flights/1").accept(MediaType.APPLICATION_JSON));

        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.flightNumber").value("AV1234"));
    }

    @DisplayName("Should return 400 when flight does not exist")
    @Test
    void shouldReturn400_getFlightById() throws Exception {
        given(flightService.getFlightById(1L)).willThrow(new FlightNotFoundException(1L));

        ResultActions response = mockMvc.perform(get("/flights/1").accept(MediaType.APPLICATION_JSON));

        response.andExpect(status().isBadRequest())
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
                get("/flights")
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
                get("/flights")
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
                post("/flights")
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
                post("/flights")
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
                post("/flights/10/reschedule")
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
                patch("/flights/1/cancel")
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
                patch("/flights/1/change-airplane-type")
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
                patch("/flights/1/change-airplane-type")
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
                post("/flights/1/reschedule")
                        .param("newDepartureLocalDateTime", pastDate.toString())
                        .accept(MediaType.APPLICATION_JSON)
        );

        response.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").value("invalid-arguments"));
    }
}
