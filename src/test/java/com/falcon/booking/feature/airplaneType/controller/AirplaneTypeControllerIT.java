package com.falcon.booking.feature.airplaneType.controller;

import com.falcon.booking.common.enums.AirplaneTypeStatus;
import com.falcon.booking.feature.airplaneType.dto.ConfigureSeatsDto;
import com.falcon.booking.feature.airplaneType.dto.CorrectAirplaneTypeDto;
import com.falcon.booking.feature.airplaneType.dto.CreateAirplaneTypeDto;
import com.falcon.booking.feature.airplaneType.dto.ResponseAirplaneTypeDto;
import com.falcon.booking.feature.airplaneType.exception.AirplaneNotFoundException;
import com.falcon.booking.feature.airplaneType.exception.InvalidSeatConfigurationException;
import com.falcon.booking.feature.airplaneType.service.AirplaneTypeService;
import com.falcon.booking.security.jwt.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WithMockUser(roles = "ADMIN")
@WebMvcTest(AirplaneTypeController.class)
class AirplaneTypeControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private AirplaneTypeService airplaneTypeService;

    @Autowired
    private ObjectMapper objectMapper;

    private ResponseAirplaneTypeDto sampleResponse() {
        return new ResponseAirplaneTypeDto(1L, "Boeing", "737", 180, 12, "ABCDEF", AirplaneTypeStatus.ACTIVE);
    }

    // ─── GET /{id} ────────────────────────────────────────────────────────────

    @DisplayName("Should return 200 OK and airplane type when id exists")
    @Test
    void shouldReturn200AndAirplaneType_getById() throws Exception {
        given(airplaneTypeService.getAirplaneTypeById(1L)).willReturn(sampleResponse());

        ResultActions response = mockMvc.perform(
                get("/v1/airplane-types/{id}", 1L)
                        .accept(MediaType.APPLICATION_JSON));

        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.producer").value("Boeing"))
                .andExpect(jsonPath("$.model").value("737"))
                .andExpect(jsonPath("$.economySeats").value(180))
                .andExpect(jsonPath("$.firstClassSeats").value(12))
                .andExpect(jsonPath("$.seatColumns").value("ABCDEF"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @DisplayName("Should return 404 Not Found when AirplaneType does not exist")
    @Test
    void shouldReturn404NotFound_getById() throws Exception {
        given(airplaneTypeService.getAirplaneTypeById(any(Long.class)))
                .willThrow(new AirplaneNotFoundException(1L));

        ResultActions response = mockMvc.perform(
                get("/v1/airplane-types/1")
                        .accept(MediaType.APPLICATION_JSON));

        response.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.type").value("airplane-type-does-not-exist"))
                .andExpect(jsonPath("$.message").exists());
    }

    // ─── GET / ────────────────────────────────────────────────────────────────

    @DisplayName("Should return 200 OK and list of airplane types")
    @Test
    void shouldReturn200AndAirplaneTypeList_getAll() throws Exception {
        List<ResponseAirplaneTypeDto> airplaneTypes = List.of(
                new ResponseAirplaneTypeDto(1L, "Boeing", "737", 180, 12, "ABCDEF", AirplaneTypeStatus.ACTIVE),
                new ResponseAirplaneTypeDto(2L, "Airbus", "A320", 160, 16, "ABCDEF", AirplaneTypeStatus.INACTIVE));

        given(airplaneTypeService.getAirplaneTypes(null, null, null)).willReturn(airplaneTypes);

        ResultActions response = mockMvc.perform(
                get("/v1/airplane-types")
                        .accept(MediaType.APPLICATION_JSON));

        response.andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.size()").value(2));
    }

    // ─── POST / ───────────────────────────────────────────────────────────────

    @DisplayName("Should return 201 Created when airplane type is created")
    @Test
    void shouldReturn201Created_createAirplaneType() throws Exception {
        CreateAirplaneTypeDto createDto = new CreateAirplaneTypeDto("Airbus", "A320", 160, 16, "ABCDEF");
        ResponseAirplaneTypeDto responseDto =
                new ResponseAirplaneTypeDto(1L, "Airbus", "A320", 160, 16, "ABCDEF", AirplaneTypeStatus.ACTIVE);

        given(airplaneTypeService.addAirplaneType(createDto)).willReturn(responseDto);

        ResultActions response = mockMvc.perform(
                post("/v1/airplane-types")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto))
                        .accept(MediaType.APPLICATION_JSON));

        response.andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.producer").value("Airbus"))
                .andExpect(jsonPath("$.model").value("A320"))
                .andExpect(jsonPath("$.economySeats").value(160))
                .andExpect(jsonPath("$.firstClassSeats").value(16))
                .andExpect(jsonPath("$.seatColumns").value("ABCDEF"));
    }

    @DisplayName("Should return 400 Bad Request when create dto is invalid (missing required fields)")
    @Test
    void shouldReturn400InvalidArguments_createAirplaneType() throws Exception {
        CreateAirplaneTypeDto invalidDto = new CreateAirplaneTypeDto("", "", null, null, null);

        ResultActions response = mockMvc.perform(
                post("/v1/airplane-types")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto))
                        .accept(MediaType.APPLICATION_JSON));

        response.andExpect(status().isBadRequest());
    }

    // ─── PATCH /{id}/configure-seats ──────────────────────────────────────────

    @DisplayName("Should return 200 OK when seat configuration is updated")
    @Test
    void shouldReturn200_configureSeats() throws Exception {
        ConfigureSeatsDto configureDto = new ConfigureSeatsDto(190, 10, "ABCDEF");
        ResponseAirplaneTypeDto responseDto =
                new ResponseAirplaneTypeDto(1L, "Boeing", "737 MAX", 190, 10, "ABCDEF", AirplaneTypeStatus.ACTIVE);

        given(airplaneTypeService.configureSeats(eq(1L), any(ConfigureSeatsDto.class))).willReturn(responseDto);

        ResultActions response = mockMvc.perform(
                patch("/v1/airplane-types/1/configure-seats")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(configureDto))
                        .accept(MediaType.APPLICATION_JSON));

        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.economySeats").value(190))
                .andExpect(jsonPath("$.firstClassSeats").value(10))
                .andExpect(jsonPath("$.seatColumns").value("ABCDEF"));
    }

    @DisplayName("Should return 422 Unprocessable Entity when seat configuration is invalid")
    @Test
    void shouldReturn422_configureSeats_invalidConfig() throws Exception {
        ConfigureSeatsDto configureDto = new ConfigureSeatsDto(11, 0, "ABCDEF");

        given(airplaneTypeService.configureSeats(eq(1L), any(ConfigureSeatsDto.class)))
                .willThrow(new InvalidSeatConfigurationException(
                        "The total number of seats (11) must be a multiple of the number of seat columns (6)."));

        ResultActions response = mockMvc.perform(
                patch("/v1/airplane-types/1/configure-seats")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(configureDto))
                        .accept(MediaType.APPLICATION_JSON));

        response.andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.type").value("invalid-seat-configuration"))
                .andExpect(jsonPath("$.message").exists());
    }

    @DisplayName("Should return 400 Bad Request when configure-seats dto has invalid fields")
    @Test
    void shouldReturn400_configureSeats_invalidDto() throws Exception {
        // seatColumns has lowercase — fails @Pattern validation before hitting service
        String invalidBody = "{\"economySeats\":100,\"firstClassSeats\":10,\"seatColumns\":\"abc\"}";

        ResultActions response = mockMvc.perform(
                patch("/v1/airplane-types/1/configure-seats")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidBody)
                        .accept(MediaType.APPLICATION_JSON));

        response.andExpect(status().isBadRequest());
    }

    // ─── PATCH /{id}/correct-identity ─────────────────────────────────────────

    @DisplayName("Should return 200 OK when airplane type identity is corrected")
    @Test
    void shouldReturn200_correctAirplaneType() throws Exception {
        CorrectAirplaneTypeDto correctDto = new CorrectAirplaneTypeDto("BOEING", "737-800");
        ResponseAirplaneTypeDto responseDto =
                new ResponseAirplaneTypeDto(1L, "BOEING", "737-800", 180, 12, "ABCDEF", AirplaneTypeStatus.ACTIVE);

        given(airplaneTypeService.correctAirplaneType(1L, correctDto)).willReturn(responseDto);

        ResultActions response = mockMvc.perform(
                patch("/v1/airplane-types/1/correct-identity")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(correctDto))
                        .accept(MediaType.APPLICATION_JSON));

        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.producer").value("BOEING"))
                .andExpect(jsonPath("$.model").value("737-800"));
    }

    // ─── PATCH /{id}/deactivate ───────────────────────────────────────────────

    @DisplayName("Should return 200 OK when airplane type is deactivated")
    @Test
    void shouldReturn200_deactivateAirplaneType() throws Exception {
        ResponseAirplaneTypeDto responseDto =
                new ResponseAirplaneTypeDto(1L, "Boeing", "737", 180, 12, "ABCDEF", AirplaneTypeStatus.INACTIVE);
        given(airplaneTypeService.deactivateAirplaneType(1L)).willReturn(responseDto);

        ResultActions response = mockMvc.perform(
                patch("/v1/airplane-types/1/deactivate")
                        .with(csrf())
                        .accept(MediaType.APPLICATION_JSON));

        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("INACTIVE"));
    }

    // ─── PATCH /{id}/activate ─────────────────────────────────────────────────

    @DisplayName("Should return 200 OK when airplane type is activated")
    @Test
    void shouldReturn200_activateAirplaneType() throws Exception {
        ResponseAirplaneTypeDto responseDto =
                new ResponseAirplaneTypeDto(1L, "Boeing", "737", 180, 12, "ABCDEF", AirplaneTypeStatus.ACTIVE);
        given(airplaneTypeService.activateAirplaneType(1L)).willReturn(responseDto);

        ResultActions response = mockMvc.perform(
                patch("/v1/airplane-types/1/activate")
                        .with(csrf())
                        .accept(MediaType.APPLICATION_JSON));

        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    // ─── PATCH /{id}/retire ───────────────────────────────────────────────────

    @DisplayName("Should return 200 OK when airplane type is retired")
    @Test
    void shouldReturn200_retireAirplaneType() throws Exception {
        ResponseAirplaneTypeDto responseDto =
                new ResponseAirplaneTypeDto(1L, "Boeing", "737", 180, 12, "ABCDEF", AirplaneTypeStatus.RETIRED);
        given(airplaneTypeService.retireAirplaneType(1L)).willReturn(responseDto);

        ResultActions response = mockMvc.perform(
                patch("/v1/airplane-types/{id}/retire", 1L)
                        .with(csrf())
                        .accept(MediaType.APPLICATION_JSON));

        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RETIRED"));
    }
}
