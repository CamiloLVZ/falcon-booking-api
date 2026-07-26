package com.falcon.booking.feature.checkIn.controller;

import com.falcon.booking.common.enums.PassengerGender;
import com.falcon.booking.common.enums.PassengerReservationStatus;
import com.falcon.booking.common.enums.SeatClass;
import com.falcon.booking.feature.boarding.service.BoardingService;
import com.falcon.booking.feature.checkIn.dto.CheckInRequestDto;
import com.falcon.booking.feature.checkIn.service.CheckInService;
import com.falcon.booking.feature.passenger.dto.ResponsePassengerDto;
import com.falcon.booking.feature.reservation.dto.ResponsePassengerReservationDto;
import com.falcon.booking.security.jwt.JwtFilter;
import com.falcon.booking.security.jwt.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CheckInController.class)
@AutoConfigureMockMvc(addFilters = false)
class CheckInControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CheckInService checkInService;

    @MockitoBean
    private BoardingService boardingService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private JwtFilter jwtFilter;
    
    @Autowired
    private ObjectMapper objectMapper;

    private ResponsePassengerReservationDto responseDto;

    @BeforeEach
    void setUp() {
        ResponsePassengerDto passengerDto = new ResponsePassengerDto(
                1L, "John", "Doe", PassengerGender.M, "123456789", LocalDate.now(), "CO", "123456789"
        );
        responseDto = new ResponsePassengerReservationDto(
                10L,
                passengerDto,
                15,
                SeatClass.ECONOMY,
                PassengerReservationStatus.CHECKED_IN
        );
    }

    @Test
    void checkInPassenger_ShouldReturn200AndIssueBoardingPass() throws Exception {
        when(checkInService.checkInByIdentificationNumber(anyString(), anyString(), anyString(), anyString(), any()))
                .thenReturn(responseDto);

        CheckInRequestDto request = new CheckInRequestDto("ABC1234", "contact@test.com", "123456789", "CO", 15);

        mockMvc.perform(post("/v1/check-in")
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.seatNumber").value(15))
                .andExpect(jsonPath("$.status").value("CHECKED_IN"));

        verify(checkInService, times(1)).checkInByIdentificationNumber("ABC1234", "contact@test.com", "123456789", "CO", 15);
        verify(boardingService, times(1)).issue(10L);
    }

    @Test
    void checkInPassenger_WithoutSeatNumber_ShouldReturn200AndIssueBoardingPass() throws Exception {
        when(checkInService.checkInByIdentificationNumber(anyString(), anyString(), anyString(), anyString(), isNull()))
                .thenReturn(responseDto);

        CheckInRequestDto request = new CheckInRequestDto("ABC1234", "contact@test.com", "123456789", "CO", null);

        mockMvc.perform(post("/v1/check-in")
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(checkInService, times(1)).checkInByIdentificationNumber("ABC1234", "contact@test.com", "123456789", "CO", null);
        verify(boardingService, times(1)).issue(10L);
    }
}
