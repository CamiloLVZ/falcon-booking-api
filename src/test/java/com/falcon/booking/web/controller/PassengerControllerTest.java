package com.falcon.booking.web.controller;

import com.falcon.booking.domain.exception.Passenger.PassengerNotFoundException;
import com.falcon.booking.domain.service.PassengerService;
import com.falcon.booking.domain.service.ReservationService;
import com.falcon.booking.domain.valueobject.PassengerGender;
import com.falcon.booking.web.dto.passenger.AddPassengerDto;
import com.falcon.booking.web.dto.passenger.ResponsePassengerDto;
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

import java.time.LocalDate;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PassengerController.class)
public class PassengerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PassengerService passengerService;

    @MockitoBean
    private ReservationService reservationService;

    private ResponsePassengerDto createPassengerDto() {
        return new ResponsePassengerDto(
                1L,
                "JUAN",
                "PEREZ",
                PassengerGender.M,
                "CO",
                LocalDate.of(1990, 1, 10),
                "AB1234",
                "10001"
        );
    }

    @DisplayName("Should return 200 OK and passenger when id exists")
    @Test
    void shouldReturn200AndPassenger_getById() throws Exception {
        ResponsePassengerDto passengerDto = createPassengerDto();
        given(passengerService.getPassengerById(1L)).willReturn(passengerDto);

        ResultActions response = mockMvc.perform(
                get("/passengers/{id}", 1L)
                        .accept(MediaType.APPLICATION_JSON));

        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.firstName").value("JUAN"))
                .andExpect(jsonPath("$.nationalityIsoCode").value("CO"));
    }

    @DisplayName("Should return 200 OK and passenger when searching by identification")
    @Test
    void shouldReturn200AndPassenger_getByIdentification() throws Exception {
        ResponsePassengerDto passengerDto = createPassengerDto();
        given(passengerService.getPassengerByIdentificationNumber("10001", "CO"))
                .willReturn(passengerDto);

        ResultActions response = mockMvc.perform(
                get("/passengers/identification")
                        .param("identificationNumber", "10001")
                        .param("countryIsoCode", "CO")
                        .accept(MediaType.APPLICATION_JSON));

        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.identificationNumber").value("10001"))
                .andExpect(jsonPath("$.passportNumber").value("AB1234"));
    }

    @DisplayName("Should return 200 OK and passenger when searching by passport")
    @Test
    void shouldReturn200AndPassenger_getByPassport() throws Exception {
        ResponsePassengerDto passengerDto = createPassengerDto();
        given(passengerService.getPassengerByPassportNumber("AB1234")).willReturn(passengerDto);

        ResultActions response = mockMvc.perform(
                get("/passengers/passport/{passportNumber}", "AB1234")
                        .accept(MediaType.APPLICATION_JSON));

        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.passportNumber").value("AB1234"));
    }

    @DisplayName("Should return 400 when identification request has invalid country length")
    @Test
    void shouldReturn400InvalidArguments_getByIdentification() throws Exception {
        ResultActions response = mockMvc.perform(
                get("/passengers/identification")
                        .param("identificationNumber", "10001")
                        .param("countryIsoCode", "COL")
                        .accept(MediaType.APPLICATION_JSON));

        response.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").value("invalid-arguments"))
                .andExpect(jsonPath("$.message").exists());
    }

    @DisplayName("Should return 200 OK and reservations when searching by passenger")
    @Test
    void shouldReturn200AndReservationList_getAllReservationsByPassenger() throws Exception {
        List<ResponseReservationDto> reservations = List.of();
        given(reservationService.getAllReservationsByPassengerIdentificationNumber("10001", "CO"))
                .willReturn(reservations);

        ResultActions response = mockMvc.perform(
                get("/passengers/reservations")
                        .param("identificationNumber", "10001")
                        .param("countryIsoCode", "CO")
                        .accept(MediaType.APPLICATION_JSON));

        response.andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.size()").value(0));
    }

    @DisplayName("Should return 201 Created when passenger is created")
    @Test
    void shouldReturn201Created_addPassenger() throws Exception {
        AddPassengerDto addPassengerDto = new AddPassengerDto(
                "Juan",
                "Perez",
                PassengerGender.M,
                "CO",
                LocalDate.of(1990, 1, 10),
                "AB1234",
                "10001"
        );
        ResponsePassengerDto responseDto = createPassengerDto();
        given(passengerService.addPassenger(addPassengerDto)).willReturn(responseDto);

        ResultActions response = mockMvc.perform(
                post("/passengers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addPassengerDto))
                        .accept(MediaType.APPLICATION_JSON));

        response.andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.passportNumber").value("AB1234"));
    }

    @DisplayName("Should return 400 Bad Request when creating passenger with invalid payload")
    @Test
    void shouldReturn400_addPassenger() throws Exception {
        AddPassengerDto invalidDto = new AddPassengerDto(
                "",
                "",
                null,
                "COL",
                LocalDate.now().plusDays(1),
                "A",
                ""
        );

        ResultActions response = mockMvc.perform(
                post("/passengers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto))
                        .accept(MediaType.APPLICATION_JSON));

        response.andExpect(status().isBadRequest());
    }

    @DisplayName("Should return 200 OK when patching passenger passport")
    @Test
    void shouldReturn200_patchPassengerPassport() throws Exception {
        ResponsePassengerDto responseDto = createPassengerDto();
        given(passengerService.updatePassengerPassport("10001", "CO", "AB9999"))
                .willReturn(new ResponsePassengerDto(
                        responseDto.id(),
                        responseDto.firstName(),
                        responseDto.lastName(),
                        responseDto.gender(),
                        responseDto.nationalityIsoCode(),
                        responseDto.dateOfBirth(),
                        "AB9999",
                        responseDto.identificationNumber()));

        ResultActions response = mockMvc.perform(
                patch("/passengers/passport")
                        .param("identificationNumber", "10001")
                        .param("countryIsoCode", "CO")
                        .param("newPassportNumber", "AB9999")
                        .accept(MediaType.APPLICATION_JSON));

        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.passportNumber").value("AB9999"));
    }

    @DisplayName("Should return 400 when passenger does not exist")
    @Test
    void shouldReturn400PassengerNotFound_getByPassport() throws Exception {
        given(passengerService.getPassengerByPassportNumber("XX0000"))
                .willThrow(new PassengerNotFoundException("XX0000"));

        ResultActions response = mockMvc.perform(
                get("/passengers/passport/{passportNumber}", "XX0000")
                        .accept(MediaType.APPLICATION_JSON));

        response.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").value("passenger-does-not-exist"))
                .andExpect(jsonPath("$.message").exists());
    }
}
