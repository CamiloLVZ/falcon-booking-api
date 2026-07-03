package com.falcon.booking.feature.flight.controller;

import com.falcon.booking.common.enums.FlightStatus;
import com.falcon.booking.feature.airplaneType.dto.AirplaneTypeInFlightDto;
import com.falcon.booking.feature.flight.dto.ResponseFlightDto;
import com.falcon.booking.feature.flight.exception.FlightNotFoundException;
import com.falcon.booking.feature.flight.service.FlightQueryService;
import com.falcon.booking.security.jwt.JwtUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WithMockUser(roles = "ADMIN")
@WebMvcTest(FlightQueryController.class)
class FlightQueryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private FlightQueryService flightQueryService;

    private ResponseFlightDto createResponseDto(Long id, String flightNumber, FlightStatus status) {
        return new ResponseFlightDto(
                id,
                flightNumber,
                "BOG",
                "MDE",
                OffsetDateTime.parse("2026-01-01T13:00:00Z"),
                LocalDateTime.parse("2026-01-01T08:00:00"),
                40,
                new AirplaneTypeInFlightDto("Airbus", "A320", 100, 10),
                status
        );
    }

    @DisplayName("Should return 200 OK and flight by id")
    @Test
    void shouldReturn200_getFlightById() throws Exception {
        ResponseFlightDto responseDto = createResponseDto(1L, "AV1234", FlightStatus.SCHEDULED);
        given(flightQueryService.getFlightById(1L)).willReturn(responseDto);

        ResultActions response = mockMvc.perform(get("/v1/flights/1").accept(MediaType.APPLICATION_JSON));

        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.flightNumber").value("AV1234"));
    }

    @DisplayName("Should return 404 when flight does not exist")
    @Test
    void shouldReturn400_getFlightById() throws Exception {
        given(flightQueryService.getFlightById(1L)).willThrow(new FlightNotFoundException(1L));

        ResultActions response = mockMvc.perform(get("/v1/flights/1").accept(MediaType.APPLICATION_JSON));

        response.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.type").value("flight-does-not-exist"));
    }

    @DisplayName("Should return 200 OK and flight list")
    @Test
    void shouldReturn200_getAllFlights() throws Exception {
        Page<ResponseFlightDto> flights = new PageImpl<>(List.of(
                createResponseDto(1L, "AV1234", FlightStatus.SCHEDULED),
                createResponseDto(2L, "AV1234", FlightStatus.CHECK_IN_AVAILABLE)
        ), PageRequest.of(0, 10), 2);

        given(flightQueryService.getAllFlights("AV1234", FlightStatus.SCHEDULED,
                LocalDate.parse("2026-01-01"), LocalDate.parse("2026-01-31"), 0, 10)).willReturn(flights);

        ResultActions response = mockMvc.perform(
                get("/v1/flights")
                        .param("flightNumber", "AV1234")
                        .param("status", "SCHEDULED")
                        .param("dateFrom", "2026-01-01")
                        .param("dateTo", "2026-01-31")
                        .param("page", "0")
                        .param("size", "10")
                        .accept(MediaType.APPLICATION_JSON)
        );

        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.size()").value(2))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @DisplayName("Should return 400 when getAllFlights has invalid flight number")
    @Test
    void shouldReturn400_getAllFlights() throws Exception {
        ResultActions response = mockMvc.perform(
                get("/v1/flights")
                        .param("flightNumber", "AV1")
                        .param("page", "0")
                        .param("size", "10")
                        .accept(MediaType.APPLICATION_JSON)
        );

        response.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").value("invalid-arguments"));
    }



    @DisplayName("Should return 200 OK with all flights paginated")
    @Test
    void shouldReturn200_getAllFlightsPaginated() throws Exception {
        Page<ResponseFlightDto> flights = new PageImpl<>(List.of(
                createResponseDto(2L, "AV1234", FlightStatus.SCHEDULED),
                createResponseDto(1L, "AV1234", FlightStatus.CHECK_IN_AVAILABLE)
        ), PageRequest.of(0, 10), 2);

        given(flightQueryService.getAllFlightsPaginated("AV1234", FlightStatus.SCHEDULED, 0, 10)).willReturn(flights);

        ResultActions response = mockMvc.perform(
                get("/v1/flights/all")
                        .param("flightNumber", "AV1234")
                        .param("status", "SCHEDULED")
                        .param("page", "0")
                        .param("size", "10")
                        .accept(MediaType.APPLICATION_JSON)
        );

        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.size()").value(2))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @DisplayName("Should return 200 OK with all flights paginated without filters")
    @Test
    void shouldReturn200_getAllFlightsPaginated_noFilters() throws Exception {
        Page<ResponseFlightDto> flights = new PageImpl<>(List.of(
                createResponseDto(1L, "AV1234", FlightStatus.SCHEDULED)
        ), PageRequest.of(0, 10), 1);

        given(flightQueryService.getAllFlightsPaginated(null, null, 0, 10)).willReturn(flights);

        ResultActions response = mockMvc.perform(
                get("/v1/flights/all")
                        .param("page", "0")
                        .param("size", "10")
                        .accept(MediaType.APPLICATION_JSON)
        );

        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(1));
    }

}







