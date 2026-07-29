package com.falcon.booking.feature.reservation.controller;

import com.falcon.booking.common.enums.*;
import com.falcon.booking.feature.airplaneType.dto.AirplaneTypeInFlightDto;
import com.falcon.booking.feature.auth.service.UserService;
import com.falcon.booking.feature.flight.dto.ResponseFlightDto;
import com.falcon.booking.feature.passenger.dto.ResponsePassengerDto;
import com.falcon.booking.feature.reservation.dto.ResponsePassengerReservationDto;
import com.falcon.booking.feature.reservation.dto.ResponseReservationDto;
import com.falcon.booking.feature.reservation.exception.InvalidReservationAccessException;
import com.falcon.booking.feature.reservation.exception.ReservationNotFoundException;
import com.falcon.booking.feature.reservation.service.ReservationAccessService;
import com.falcon.booking.feature.reservation.service.ReservationCommandService;
import com.falcon.booking.feature.reservation.service.ReservationQueryService;
import com.falcon.booking.security.jwt.JwtPayload;
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
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WithMockUser(roles = "ADMIN")
@WebMvcTest(ReservationController.class)
class ReservationControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private ReservationQueryService reservationQueryService;

    @MockitoBean
    private ReservationCommandService reservationCommandService;

    @MockitoBean
    private ReservationAccessService reservationAccessService;

    @MockitoBean
    private UserService userService;

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
                new AirplaneTypeInFlightDto("Airbus", "A320", 100, 10, "ABCDEF"),
                FlightStatus.SCHEDULED,
                BigDecimal.valueOf(100.0),
                BigDecimal.valueOf(200.0)
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
                List.of(new ResponsePassengerReservationDto(null, passenger, 12, null, SeatClass.ECONOMY, PassengerReservationStatus.RESERVED))
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

    @DisplayName("Should require contact email when a guest gets a complete reservation")
    @Test
    void shouldRequireContactEmail_getReservationByNumberForGuest() {
        ReservationController controller = new ReservationController(
                reservationQueryService, reservationCommandService, reservationAccessService, userService);

        assertThatThrownBy(() -> controller.getReservation("ABC123", null, null))
                .isInstanceOf(InvalidReservationAccessException.class);
    }

    @DisplayName("Should verify contact email when a guest gets a complete reservation")
    @Test
    void shouldVerifyContactEmail_getReservationByNumberForGuest() {
        ResponseReservationDto dto = createResponseReservationDto("ABC123", ReservationStatus.RESERVED);
        given(reservationQueryService.getReservationByNumber("ABC123")).willReturn(dto);

        ReservationController controller = new ReservationController(
                reservationQueryService, reservationCommandService, reservationAccessService, userService);
        ResponseEntity<ResponseReservationDto> response = controller.getReservation("ABC123", "contact@test.com", null);

        assertThat(response.getBody()).isEqualTo(dto);
        then(reservationAccessService).should().getReservationByNumberAndContactEmail("ABC123", "contact@test.com");
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



    @DisplayName("Should return 200 OK when canceling passenger by identification")
    @Test
    void shouldReturn200_cancelPassengerByIdentification() throws Exception {
        ResponseReservationDto dto = createResponseReservationDto("ABC123", ReservationStatus.RESERVED);
        given(reservationCommandService.cancelPassengerReservationByIdentificationNumber("ABC123", "contact@test.com", "110011", "CO"))
                .willReturn(dto);

        ResultActions response = mockMvc.perform(
                patch("/v1/reservations/ABC123/cancel/passenger")
                       .with(csrf())
                        .param("identificationNumber", "110011")
                        .param("countryIsoCode", "CO")
                        .content("{\"contactEmail\":\"contact@test.com\"}")
                        .contentType(MediaType.APPLICATION_JSON)
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
                        .content("{\"contactEmail\":\"contact@test.com\"}")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
        );

        response.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").value("invalid-arguments"));
    }

    @DisplayName("Should return 200 OK when canceling reservation")
    @Test
    void shouldReturn200_cancelReservation() throws Exception {
        ResponseReservationDto dto = createResponseReservationDto("ABC123", ReservationStatus.CANCELED);
        given(reservationCommandService.cancelReservationByContactEmail("ABC123", "contact@test.com")).willReturn(dto);

        ResultActions response = mockMvc.perform(
                patch("/v1/reservations/ABC123/cancel")
                       .with(csrf())
                        .content("{\"contactEmail\":\"contact@test.com\"}")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
        );

        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.number").value("ABC123"))
                .andExpect(jsonPath("$.status").value("CANCELED"));
    }

    @DisplayName("Should return 200 OK when canceling passenger by passport")
    @Test
    void shouldReturn200_cancelPassengerByPassport() throws Exception {
        ResponseReservationDto dto = createResponseReservationDto("ABC123", ReservationStatus.RESERVED);
        given(reservationCommandService.cancelPassengerReservationByPassportNumber("ABC123", "contact@test.com", "P123456"))
                .willReturn(dto);

        ResultActions response = mockMvc.perform(
                patch("/v1/reservations/ABC123/cancel/passenger/P123456")
                       .with(csrf())
                        .content("{\"contactEmail\":\"contact@test.com\"}")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
        );

        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.number").value("ABC123"));
    }

    @DisplayName("Should return 200 OK when getting my reservations")
    @Test
    void shouldReturn200_getMyReservations() throws Exception {
        ResponseReservationDto dto = createResponseReservationDto("ABC123", ReservationStatus.RESERVED);
        Page<ResponseReservationDto> reservations = new PageImpl<>(List.of(dto), PageRequest.of(0, 10), 1);
        given(reservationQueryService.getMyReservations(any(), eq(null), eq(0), eq(10)))
                .willReturn(reservations);
        JwtPayload payload = new JwtPayload(1L, "client@test.com", List.of("CLIENT"));
        Authentication auth = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                payload, null, List.of(new SimpleGrantedAuthority("ROLE_CLIENT")));

        ResultActions response = mockMvc.perform(
                get("/v1/reservations/me")
                        .with(authentication(auth))
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

    @DisplayName("Should return 200 OK when getting my reservations filtered by status")
    @Test
    void shouldReturn200_getMyReservations_filteredByStatus() throws Exception {
        ResponseReservationDto dto = createResponseReservationDto("ABC123", ReservationStatus.RESERVED);
        Page<ResponseReservationDto> reservations = new PageImpl<>(List.of(dto), PageRequest.of(0, 10), 1);
        given(reservationQueryService.getMyReservations(any(), eq(ReservationStatus.RESERVED), eq(0), eq(10)))
                .willReturn(reservations);
        JwtPayload payload = new JwtPayload(1L, "client@test.com", List.of("CLIENT"));
        Authentication auth = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                payload, null, List.of(new SimpleGrantedAuthority("ROLE_CLIENT")));

        ResultActions response = mockMvc.perform(
                get("/v1/reservations/me")
                        .with(authentication(auth))
                        .param("status", "RESERVED")
                        .param("page", "0")
                        .param("size", "10")
                        .accept(MediaType.APPLICATION_JSON));

        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.content.size()").value(1))
                .andExpect(jsonPath("$.content[0].status").value("RESERVED"));
    }

    @DisplayName("Should return 200 OK and reservation details by id")
    @Test
    void shouldReturn200_getReservationWithDetails() throws Exception {
        ResponseReservationDto dto = createResponseReservationDto("ABC123", ReservationStatus.RESERVED);
        given(reservationQueryService.getReservationById(1L)).willReturn(dto);

        ResultActions response = mockMvc.perform(get("/v1/reservations/1/with-details").accept(MediaType.APPLICATION_JSON));

        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.number").value("ABC123"))
                .andExpect(jsonPath("$.flight.flightNumber").value("AV1234"))
                .andExpect(jsonPath("$.passengers").isArray())
                .andExpect(jsonPath("$.passengers[0].passenger.firstName").value("ANA"));
    }

    @DisplayName("Should return 404 when reservation details by id does not exist")
    @Test
    void shouldReturn404_getReservationWithDetails() throws Exception {
        given(reservationQueryService.getReservationById(99L)).willThrow(new ReservationNotFoundException(99L));

        ResultActions response = mockMvc.perform(get("/v1/reservations/99/with-details").accept(MediaType.APPLICATION_JSON));

        response.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.type").value("reservation-does-not-exist"));
    }
}







