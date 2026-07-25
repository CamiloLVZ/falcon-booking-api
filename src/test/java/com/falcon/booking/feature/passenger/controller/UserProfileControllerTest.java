package com.falcon.booking.feature.passenger.controller;

import com.falcon.booking.common.enums.PassengerGender;
import com.falcon.booking.feature.auth.service.UserService;
import com.falcon.booking.feature.passenger.dto.AddPassengerDto;
import com.falcon.booking.feature.passenger.dto.ResponsePassengerDto;
import com.falcon.booking.feature.passenger.exception.PassengerProfileAlreadyLinkedException;
import com.falcon.booking.feature.passenger.exception.PassengerProfileNotFoundException;
import com.falcon.booking.feature.passenger.service.PassengerService;
import com.falcon.booking.feature.passenger.web.PassengerExceptionHandler;
import com.falcon.booking.security.jwt.JwtPayload;
import com.falcon.booking.security.jwt.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({UserProfileController.class, PassengerExceptionHandler.class})
class UserProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PassengerService passengerService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtUtil jwtUtil;

    private Authentication clientAuth;

    @BeforeEach
    void setUp() {
        JwtPayload payload = new JwtPayload(1L, "client@test.com", List.of("CLIENT"));
        clientAuth = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                payload, null, List.of(new SimpleGrantedAuthority("ROLE_CLIENT")));
    }

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

    @DisplayName("Should return 200 OK when getting my profile")
    @Test
    void shouldReturn200_getMyProfile() throws Exception {
        ResponsePassengerDto passengerDto = createPassengerDto();
        given(passengerService.getMyProfile(any())).willReturn(passengerDto);

        ResultActions response = mockMvc.perform(
                get("/v1/passengers/me")
                        .with(authentication(clientAuth))
                        .accept(MediaType.APPLICATION_JSON));

        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.firstName").value("JUAN"))
                .andExpect(jsonPath("$.nationalityIsoCode").value("CO"));
    }

    @DisplayName("Should return 404 when profile not found")
    @Test
    void shouldReturn404_getMyProfile_notFound() throws Exception {
        given(passengerService.getMyProfile(any())).willThrow(new PassengerProfileNotFoundException());

        ResultActions response = mockMvc.perform(
                get("/v1/passengers/me")
                        .with(authentication(clientAuth))
                        .accept(MediaType.APPLICATION_JSON));

        response.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.type").value("passenger-profile-not-found"));
    }

    @DisplayName("Should return 201 when creating my profile")
    @Test
    void shouldReturn201_createMyProfile() throws Exception {
        AddPassengerDto addDto = new AddPassengerDto(
                "Juan", "Perez", PassengerGender.M, "CO",
                LocalDate.of(1990, 1, 10), "AB1234", "10001");
        ResponsePassengerDto responseDto = createPassengerDto();
        given(passengerService.createMyProfile(any(), any(AddPassengerDto.class))).willReturn(responseDto);

        ResultActions response = mockMvc.perform(
                post("/v1/passengers/me")
                        .with(authentication(clientAuth))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addDto))
                        .accept(MediaType.APPLICATION_JSON));

        response.andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.firstName").value("JUAN"));
    }

    @DisplayName("Should return 409 when profile already linked")
    @Test
    void shouldReturn409_createMyProfile_alreadyLinked() throws Exception {
        AddPassengerDto addDto = new AddPassengerDto(
                "Juan", "Perez", PassengerGender.M, "CO",
                LocalDate.of(1990, 1, 10), "AB1234", "10001");
        given(passengerService.createMyProfile(any(), any(AddPassengerDto.class)))
                .willThrow(new PassengerProfileAlreadyLinkedException());

        ResultActions response = mockMvc.perform(
                post("/v1/passengers/me")
                        .with(authentication(clientAuth))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addDto))
                        .accept(MediaType.APPLICATION_JSON));

        response.andExpect(status().isConflict())
                .andExpect(jsonPath("$.type").value("passenger-profile-already-linked"));
    }

    @DisplayName("Should return 200 when updating my profile")
    @Test
    void shouldReturn200_updateMyProfile() throws Exception {
        AddPassengerDto updateDto = new AddPassengerDto(
                "Juan", "Perez", PassengerGender.M, "CO",
                LocalDate.of(1990, 1, 10), "AB1234", "10001");
        ResponsePassengerDto responseDto = createPassengerDto();
        given(passengerService.updateMyProfile(any(), any(AddPassengerDto.class))).willReturn(responseDto);

        ResultActions response = mockMvc.perform(
                put("/v1/passengers/me")
                        .with(authentication(clientAuth))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto))
                        .accept(MediaType.APPLICATION_JSON));

        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.firstName").value("JUAN"));
    }

    @DisplayName("Should return 404 when updating profile without linked passenger")
    @Test
    void shouldReturn404_updateMyProfile_notFound() throws Exception {
        AddPassengerDto updateDto = new AddPassengerDto(
                "Juan", "Perez", PassengerGender.M, "CO",
                LocalDate.of(1990, 1, 10), "AB1234", "10001");
        given(passengerService.updateMyProfile(any(), any(AddPassengerDto.class)))
                .willThrow(new PassengerProfileNotFoundException());

        ResultActions response = mockMvc.perform(
                put("/v1/passengers/me")
                        .with(authentication(clientAuth))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto))
                        .accept(MediaType.APPLICATION_JSON));

        response.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.type").value("passenger-profile-not-found"));
    }

    @DisplayName("Should return 400 when creating profile with invalid payload")
    @Test
    void shouldReturn400_createMyProfile_invalidPayload() throws Exception {
        AddPassengerDto invalidDto = new AddPassengerDto(
                "", "", null, "COL",
                LocalDate.now().plusDays(1), "A", "");

        ResultActions response = mockMvc.perform(
                post("/v1/passengers/me")
                        .with(authentication(clientAuth))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto))
                        .accept(MediaType.APPLICATION_JSON));

        response.andExpect(status().isBadRequest());
    }
}