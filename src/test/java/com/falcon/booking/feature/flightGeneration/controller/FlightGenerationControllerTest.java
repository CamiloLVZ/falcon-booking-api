package com.falcon.booking.feature.flightGeneration.controller;

import com.falcon.booking.common.enums.FlightGenerationStatus;
import com.falcon.booking.common.enums.FlightGenerationType;
import com.falcon.booking.feature.flightGeneration.dto.ResponseFlightsGenerationDto;
import com.falcon.booking.feature.flightGeneration.exception.FlightGenerationNotFoundException;
import com.falcon.booking.feature.flightGeneration.service.FlightGenerationService;
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

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WithMockUser(roles = "ADMIN")
@WebMvcTest(controllers = FlightGenerationController.class)
public class FlightGenerationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private FlightGenerationService flightGenerationService;


    private ResponseFlightsGenerationDto createResponseFlightGenerationDto(Long id, FlightGenerationStatus status) {
        return new ResponseFlightsGenerationDto(
                id, status, FlightGenerationType.ROUTE, 1L, 400,
                Instant.now(), Instant.now().plusMillis(1500), 10L, "/v1/flight-generations/"+id
        );
    }

    @DisplayName("Should return 200 OK and flight generation by id")
    @Test
    void shouldReturn200_getFlightGenerationById() throws Exception {
        ResponseFlightsGenerationDto responseDto = createResponseFlightGenerationDto(1L, FlightGenerationStatus.RUNNING);
        given(flightGenerationService.getFlightGeneration(1L)).willReturn(responseDto);

        ResultActions response = mockMvc.perform(get("/v1/flights/generations/1").accept(MediaType.APPLICATION_JSON));

        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.generationId").value(1L))
                .andExpect(jsonPath("$.status").value("RUNNING"));
    }

    @DisplayName("Should return 404 when flight does not exist")
    @Test
    void shouldReturn400_getFlightGeneration() throws Exception {
        given(flightGenerationService.getFlightGeneration(1L)).willThrow(new FlightGenerationNotFoundException(1L));

        ResultActions response = mockMvc.perform(get("/v1/flights/generations/1").accept(MediaType.APPLICATION_JSON));

        response.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.type").value("flight-generation-does-not-exist"));
    }

}
