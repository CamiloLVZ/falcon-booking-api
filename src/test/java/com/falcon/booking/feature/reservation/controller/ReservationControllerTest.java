package com.falcon.booking.feature.reservation.controller;

import com.falcon.booking.common.enums.FlightStatus;
import com.falcon.booking.common.enums.PassengerGender;
import com.falcon.booking.common.enums.PassengerReservationStatus;
import com.falcon.booking.common.enums.ReservationStatus;
import com.falcon.booking.common.enums.SeatClass;
import com.falcon.booking.feature.airplaneType.dto.AirplaneTypeInFlightDto;
import com.falcon.booking.feature.flight.dto.ResponseFlightDto;
import com.falcon.booking.feature.passenger.dto.AddPassengerDto;
import com.falcon.booking.feature.passenger.dto.ResponsePassengerDto;
import com.falcon.booking.feature.reservation.dto.AddPassengerReservationDto;
import com.falcon.booking.feature.reservation.dto.AddReservationDto;
import com.falcon.booking.feature.reservation.dto.ResponsePassengerReservationDto;
import com.falcon.booking.feature.reservation.dto.ResponseReservationDto;
import com.falcon.booking.feature.reservation.exception.ReservationNotFoundException;
import com.falcon.booking.feature.reservation.service.ReservationCommandService;
import com.falcon.booking.feature.reservation.service.ReservationQueryService;
import com.falcon.booking.security.jwt.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WithMockUser(roles = "ADMIN")
@WebMvcTest(ReservationController.class)
class ReservationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private ReservationQueryService reservationQueryService;

    @MockitoBean
    private ReservationCommandService reservationCommandService;



    @Autowired
    private ObjectMapper objectMapper;

    private ResponseReservationDto createResponseReservationDto(String number, ReservationStatus status) {
        ResponseFlightDto flight = new ResponseFlightDto(
                10L,
                "AV1234",
                "BOG",
                "MDE",
                OffsetDateTime.parse("2026-01-01T13:00:00Z"),
                LocalDateTime.parse("2026-01-01T08:00:00"),
                40,
                new AirplaneTypeInFlightDto("Airbus", "A320", 100, 10),
                FlightStatus.SCHEDULED
        );

        ResponsePassengerDto passenger = new ResponsePassengerDto(
                8L,
                "ANA",
                "PEREZ",
                PassengerGender.F,
                "CO",
                LocalDate.parse("1998-05-10"),
                "P123456",
                "110011"
        );

        return new ResponseReservationDto(
                number,
                "contact@test.com",
                Instant.parse("2026-01-01T12:00:00Z"),
                status,
                flight,
                List.of(new ResponsePassengerReservationDto(passenger, 12, SeatClass.ECONOMY, PassengerReservationStatus.RESERVED))
        );
    }

    @DisplayName("Should return 200 OK and reservation by number")
    @Test
    void shouldReturn200_getReservationByNumber() throws Exception {
        ResponseReservationDto dto = createResponseReservationDto("ABC123", ReservationStatus.RESERVED);
        given(reservationQueryService.getReservationByNumber("ABC123")).willReturn(dto);

        ResultActions response = mockMvc.perform(get("/v1/reservations/ABC123").accept(MediaType.APPLICATION_JSON));

        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.number").value("ABC123"))
                .andExpect(jsonPath("$.status").value("RESERVED"));
    }

    @DisplayName("Should return 404 when reservation does not exist")
    @Test
    void shouldReturn404_getReservationByNumber() throws Exception {
        given(reservationQueryService.getReservationByNumber("ABC123")).willThrow(new ReservationNotFoundException("ABC123"));

        ResultActions response = mockMvc.perform(get("/v1/reservations/ABC123").accept(MediaType.APPLICATION_JSON));

        response.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.type").value("reservation-does-not-exist"));
    }

    @DisplayName("Should return 200 OK and paginated reservations by flight")
    @Test
    void shouldReturn200_getAllReservationsByFlight() throws Exception {
        ResponseReservationDto dto = createResponseReservationDto("ABC123", ReservationStatus.RESERVED);
        Page<ResponseReservationDto> reservations = new PageImpl<>(List.of(dto), PageRequest.of(0, 10), 1);
        given(reservationQueryService.getAllReservationsByFlight(10L, 0, 10)).willReturn(reservations);

        ResultActions response = mockMvc.perform(
                get("/v1/reservations/flight/10")
                        .param("page", "0")
                        .param("size", "10")
                        .accept(MediaType.APPLICATION_JSON));

        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.size()").value(1))
                .andExpect(jsonPath("$.content[0].number").value("ABC123"))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @DisplayName("Should return 201 Created when reservation is added")
    @Test
    void shouldReturn201_addReservation() throws Exception {
        AddPassengerDto passenger = new AddPassengerDto("Ana", "Perez", PassengerGender.F, "CO",
                LocalDate.now().minusYears(25), "P123456", "110011");
        AddReservationDto request = new AddReservationDto(10L, "contact@test.com",
                List.of(new AddPassengerReservationDto(passenger, 12, SeatClass.ECONOMY)));
        ResponseReservationDto responseDto = createResponseReservationDto("ABC123", ReservationStatus.RESERVED);
        given(reservationCommandService.addReservation(request)).willReturn(responseDto);

        ResultActions response = mockMvc.perform(
                post("/v1/reservations")
                       .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .accept(MediaType.APPLICATION_JSON)
        );

        response.andExpect(status().isCreated())
                .andExpect(jsonPath("$.number").value("ABC123"));
    }

    @DisplayName("Should return 400 when add reservation payload is invalid")
    @Test
    void shouldReturn400_addReservation() throws Exception {
        AddReservationDto invalid = new AddReservationDto(null, "mail-invalido",
                List.of());

        ResultActions response = mockMvc.perform(
                post("/v1/reservations")
                       .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid))
                        .accept(MediaType.APPLICATION_JSON)
        );

        response.andExpect(status().isBadRequest());
    }

    @DisplayName("Should return 200 OK when canceling passenger by identification")
    @Test
    void shouldReturn200_cancelPassengerByIdentification() throws Exception {
        ResponseReservationDto dto = createResponseReservationDto("ABC123", ReservationStatus.RESERVED);
        given(reservationCommandService.cancelPassengerReservationByIdentificationNumber("ABC123", "110011", "CO"))
                .willReturn(dto);

        ResultActions response = mockMvc.perform(
                patch("/v1/reservations/ABC123/cancel/passenger")
                       .with(csrf())
                        .param("identificationNumber", "110011")
                        .param("countryIsoCode", "CO")
                        .accept(MediaType.APPLICATION_JSON)
        );

        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.number").value("ABC123"));
    }

    @DisplayName("Should return 400 when country iso code has invalid length")
    @Test
    void shouldReturn400_cancelPassengerByIdentification() throws Exception {
        ResultActions response = mockMvc.perform(
                patch("/v1/reservations/ABC123/cancel/passenger")
                       .with(csrf())
                        .param("identificationNumber", "110011")
                        .param("countryIsoCode", "COL")
                        .accept(MediaType.APPLICATION_JSON)
        );

        response.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").value("invalid-arguments"));
    }
}







