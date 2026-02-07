package com.falcon.booking.web.controller;

import com.falcon.booking.domain.exception.AirplaneType.AirplaneNotFoundException;
import com.falcon.booking.domain.service.AirplaneTypeService;
import com.falcon.booking.domain.valueobject.AirplaneTypeStatus;
import com.falcon.booking.web.dto.airplaneType.CorrectAirplaneTypeDto;
import com.falcon.booking.web.dto.airplaneType.CreateAirplaneTypeDto;
import com.falcon.booking.web.dto.airplaneType.ResponseAirplaneTypeDto;
import com.falcon.booking.web.dto.airplaneType.UpdateAirplaneTypeDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AirplaneTypeController.class)
class AirplaneTypeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AirplaneTypeService airplaneTypeService;

    @Autowired
    private ObjectMapper objectMapper;

    @DisplayName("Should return 200 OK and airplane type when id exists")
    @Test
    void shouldReturn200AndAirplaneType_getById() throws Exception {
        ResponseAirplaneTypeDto responseDto =
                new ResponseAirplaneTypeDto(
                        1L,
                        "Boeing",
                        "737",
                        180,
                        12,
                        AirplaneTypeStatus.ACTIVE
                );

        given(airplaneTypeService.getAirplaneTypeById(1L))
                .willReturn(responseDto);

        ResultActions response = mockMvc.perform(
                get("/airplane-types/{id}", 1L)
                        .accept(MediaType.APPLICATION_JSON));

        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.producer").value("Boeing"))
                .andExpect(jsonPath("$.model").value("737"))
                .andExpect(jsonPath("$.economySeats").value(180))
                .andExpect(jsonPath("$.firstClassSeats").value(12))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @DisplayName("Should return 404 Not Found when AirplaneType does not exist")
    @Test
    void shouldReturn404NotFound_getById() throws Exception {
        given(airplaneTypeService.getAirplaneTypeById(any(Long.class)))
                .willThrow(new AirplaneNotFoundException(1L));

        ResultActions response = mockMvc.perform(
                get("/airplane-types/{id}", 1L)
                        .accept(MediaType.APPLICATION_JSON));

        response.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.type").value("airplane-type-does-not-exist"))
                .andExpect(jsonPath("$.message").exists());
    }


    @DisplayName("Should return 200 OK and list of airplane types")
    @Test
    void shouldReturn200AndAirplaneTypeList_getAll() throws Exception {
        List<ResponseAirplaneTypeDto> airplaneTypes = List.of(
                new ResponseAirplaneTypeDto(1L, "Boeing", "737", 180, 12, AirplaneTypeStatus.ACTIVE),
                new ResponseAirplaneTypeDto(2L, "Airbus", "A320", 160, 16, AirplaneTypeStatus.INACTIVE));

        given(airplaneTypeService.getAirplaneTypes(null, null, null))
                .willReturn(airplaneTypes);

        ResultActions response = mockMvc.perform(
                get("/airplane-types")
                        .accept(MediaType.APPLICATION_JSON));

        response.andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.size()").value(2));
    }

    @DisplayName("Should return 201 Created when airplane type is created")
    @Test
    void shouldReturn201Created_createAirplaneType() throws Exception {
        CreateAirplaneTypeDto createDto =
                new CreateAirplaneTypeDto("Airbus", "A320", 160, 16);

        ResponseAirplaneTypeDto responseDto = new ResponseAirplaneTypeDto(
                        1L,
                        "Airbus",
                        "A320",
                        160,
                        16,
                        AirplaneTypeStatus.ACTIVE);

        given(airplaneTypeService.addAirplaneType(createDto)).willReturn(responseDto);

        ResultActions response = mockMvc.perform(
                post("/airplane-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto))
                        .accept(MediaType.APPLICATION_JSON));

        response.andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.producer").value("Airbus"))
                .andExpect(jsonPath("$.model").value("A320"))
                .andExpect(jsonPath("$.economySeats").value(160))
                .andExpect(jsonPath("$.firstClassSeats").value(16));
    }

    @DisplayName("Should return 400 Bad Request when create dto is invalid")
    @Test
    void shouldReturn400InvalidArguments_createAirplaneType() throws Exception {
        CreateAirplaneTypeDto invalidDto =
                new CreateAirplaneTypeDto("", "", null, null);

        ResultActions response = mockMvc.perform(
                post("/airplane-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto))
                        .accept(MediaType.APPLICATION_JSON));

        response.andExpect(status().isBadRequest());
    }

    @DisplayName("Should return 200 OK when airplane type is updated")
    @Test
    void shouldReturn200_updateAirplaneType() throws Exception {
        UpdateAirplaneTypeDto updateDto = new UpdateAirplaneTypeDto( 190, 10);
        ResponseAirplaneTypeDto responseDto = new ResponseAirplaneTypeDto(
                        1L,
                        "Boeing",
                        "737 MAX",
                        190,
                        10,
                        AirplaneTypeStatus.ACTIVE);
        given(airplaneTypeService.updateAirplaneType(1L, updateDto)).willReturn(responseDto);

        ResultActions response = mockMvc.perform(
                put("/airplane-types/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto))
                        .accept(MediaType.APPLICATION_JSON));

        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.model").value("737 MAX"))
                .andExpect(jsonPath("$.economySeats").value(190))
                .andExpect(jsonPath("$.firstClassSeats").value(10));
    }

    @DisplayName("Should return 200 OK when airplane type identity is corrected")
    @Test
    void shouldReturn200_correctAirplaneType() throws Exception {
        CorrectAirplaneTypeDto correctDto = new CorrectAirplaneTypeDto("BOEING", "737-800");

        ResponseAirplaneTypeDto responseDto = new ResponseAirplaneTypeDto(
                        1L,
                        "BOEING",
                        "737-800",
                        180,
                        12,
                        AirplaneTypeStatus.ACTIVE);

        given(airplaneTypeService.correctAirplaneType(1L, correctDto))
                .willReturn(responseDto);

        ResultActions response = mockMvc.perform(
                put("/airplane-types/{id}/correct-identity", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(correctDto))
                        .accept(MediaType.APPLICATION_JSON));

        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.producer").value("BOEING"))
                .andExpect(jsonPath("$.model").value("737-800"));
    }

    @DisplayName("Should return 200 OK when airplane type is deactivated")
    @Test
    void shouldReturn200_deactivateAirplaneType() throws Exception {
        ResponseAirplaneTypeDto responseDto = new ResponseAirplaneTypeDto(
                        1L,
                        "Boeing",
                        "737",
                        180,
                        12,
                        AirplaneTypeStatus.INACTIVE);

        given(airplaneTypeService.deactivateAirplaneType(1L)).willReturn(responseDto);

        ResultActions response = mockMvc.perform(
                put("/airplane-types/{id}/deactivate", 1L)
                        .accept(MediaType.APPLICATION_JSON));

        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("INACTIVE"));
    }

    @DisplayName("Should return 200 OK when airplane type is activated")
    @Test
    void shouldReturn200_activateAirplaneType() throws Exception {
        ResponseAirplaneTypeDto responseDto = new ResponseAirplaneTypeDto(
                        1L,
                        "Boeing",
                        "737",
                        180,
                        12,
                        AirplaneTypeStatus.ACTIVE);

        given(airplaneTypeService.activateAirplaneType(1L)).willReturn(responseDto);

        ResultActions response = mockMvc.perform(
                put("/airplane-types/{id}/activate", 1L)
                        .accept(MediaType.APPLICATION_JSON));

        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @DisplayName("Should return 200 OK when airplane type is retired")
    @Test
    void shouldReturn200_retireAirplaneType() throws Exception {
        ResponseAirplaneTypeDto responseDto = new ResponseAirplaneTypeDto(
                        1L,
                        "Boeing",
                        "737",
                        180,
                        12,
                        AirplaneTypeStatus.RETIRED);

        given(airplaneTypeService.retireAirplaneType(1L)).willReturn(responseDto);

        ResultActions response = mockMvc.perform(
                put("/airplane-types/{id}/retire", 1L)
                        .accept(MediaType.APPLICATION_JSON));

        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RETIRED"));
    }

}
