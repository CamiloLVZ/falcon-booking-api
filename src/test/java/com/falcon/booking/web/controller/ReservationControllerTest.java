package com.falcon.booking.web.controller;

import com.falcon.booking.domain.exception.Reservation.ReservationNotFoundException;
import com.falcon.booking.domain.service.ReservationService;
import com.falcon.booking.domain.valueobject.FlightStatus;
import com.falcon.booking.domain.valueobject.PassengerGender;
import com.falcon.booking.domain.valueobject.PassengerReservationStatus;
import com.falcon.booking.domain.valueobject.ReservationStatus;
import com.falcon.booking.web.dto.airplaneType.AirplaneTypeInFlightDto;
import com.falcon.booking.web.dto.flight.ResponseFlightDto;
import com.falcon.booking.web.dto.passenger.AddPassengerDto;
import com.falcon.booking.web.dto.passenger.ResponsePassengerDto;
import com.falcon.booking.web.dto.reservation.AddPassengerReservationDto;
import com.falcon.booking.web.dto.reservation.AddReservationDto;
import com.falcon.booking.web.dto.reservation.ResponsePassengerReservationDto;
import com.falcon.booking.web.dto.reservation.ResponseReservationDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
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

@WebMvcTest(ReservationController.class)
class ReservationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReservationService reservationService;

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
                List.of(new ResponsePassengerReservationDto(passenger, 12, PassengerReservationStatus.RESERVED))
        );
    }

    @DisplayName("Should return 200 OK and reservation by number")
    @Test
    void shouldReturn200_getReservationByNumber() throws Exception {
        ResponseReservationDto dto = createResponseReservationDto("ABC123", ReservationStatus.RESERVED);
        given(reservationService.getReservationByNumber("ABC123")).willReturn(dto);

        ResultActions response = mockMvc.perform(get("/reservations/ABC123").accept(MediaType.APPLICATION_JSON));

        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.number").value("ABC123"))
                .andExpect(jsonPath("$.status").value("RESERVED"));
    }

    @DisplayName("Should return 404 when reservation does not exist")
    @Test
    void shouldReturn404_getReservationByNumber() throws Exception {
        given(reservationService.getReservationByNumber("ABC123")).willThrow(new ReservationNotFoundException("ABC123"));

        ResultActions response = mockMvc.perform(get("/reservations/ABC123").accept(MediaType.APPLICATION_JSON));

        response.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.type").value("reservation-does-not-exist"));
    }

    @DisplayName("Should return 201 Created when reservation is added")
    @Test
    void shouldReturn201_addReservation() throws Exception {
        AddPassengerDto passenger = new AddPassengerDto("Ana", "Perez", PassengerGender.F, "CO",
                LocalDate.now().minusYears(25), "P123456", "110011");
        AddReservationDto request = new AddReservationDto(10L, "contact@test.com",
                List.of(new AddPassengerReservationDto(passenger, 12)));
        ResponseReservationDto responseDto = createResponseReservationDto("ABC123", ReservationStatus.RESERVED);
        given(reservationService.addReservation(request)).willReturn(responseDto);

        ResultActions response = mockMvc.perform(
                post("/reservations")
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
                post("/reservations")
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
        given(reservationService.cancelPassengerReservationByIdentificationNumber("ABC123", "110011", "CO"))
                .willReturn(dto);

        ResultActions response = mockMvc.perform(
                patch("/reservations/ABC123/cancel/passenger")
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
                patch("/reservations/ABC123/cancel/passenger")
                        .param("identificationNumber", "110011")
                        .param("countryIsoCode", "COL")
                        .accept(MediaType.APPLICATION_JSON)
        );

        response.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").value("invalid-arguments"));
    }
}
