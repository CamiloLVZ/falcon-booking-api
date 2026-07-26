package com.falcon.booking.feature.route.controller;

import com.falcon.booking.feature.route.dto.AddRouteScheduleRequestDto;
import com.falcon.booking.feature.route.dto.RouteWithSchedulesDto;
import com.falcon.booking.feature.route.service.RouteSchedulesService;
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

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WithMockUser(roles = "ADMIN")
@WebMvcTest(RouteScheduleController.class)
class RouteScheduleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private RouteSchedulesService routeSchedulesService;

    @DisplayName("Should return 200 OK and route with schedules")
    @Test
    void shouldReturn200AndRouteWithSchedules_getRouteSchedules() throws Exception {
        RouteWithSchedulesDto dto = new RouteWithSchedulesDto("AV1234", List.of(DayOfWeek.MONDAY), List.of(LocalTime.of(8, 0)));
        given(routeSchedulesService.getRouteWithSchedules("AV1234")).willReturn(dto);

        ResultActions response = mockMvc.perform(get("/v1/routes/AV1234/schedules").accept(MediaType.APPLICATION_JSON));

        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.flightNumber").value("AV1234"))
                .andExpect(jsonPath("$.daysOfWeek[0]").value("MONDAY"))
                .andExpect(jsonPath("$.schedules[0]").value("08:00:00"));
    }

    @DisplayName("Should return 400 invalid-arguments when flight number size is invalid on GET")
    @Test
    void shouldReturn400InvalidArguments_getRouteSchedules() throws Exception {
        ResultActions response = mockMvc.perform(get("/v1/routes/AV1/schedules").accept(MediaType.APPLICATION_JSON));

        response.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").value("invalid-arguments"));
    }

    @DisplayName("Should return 200 OK when route schedules are set")
    @Test
    void shouldReturn200_setRouteOperatingSchedules() throws Exception {
        Set<LocalTime> schedules = Set.of(LocalTime.of(10, 0), LocalTime.of(15, 0));
        Set<DayOfWeek> daysOfWeek = Set.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY);
        AddRouteScheduleRequestDto requestDto = new AddRouteScheduleRequestDto(schedules, daysOfWeek);
        RouteWithSchedulesDto responseDto = new RouteWithSchedulesDto("AV1234", daysOfWeek, schedules);

        given(routeSchedulesService.setRouteOperatingSchedules("AV1234", requestDto)).willReturn(responseDto);

        ResultActions response = mockMvc.perform(
                patch("/v1/routes/AV1234/schedules")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto))
                        .accept(MediaType.APPLICATION_JSON));

        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.flightNumber").value("AV1234"))
                .andExpect(jsonPath("$.daysOfWeek").isArray())
                .andExpect(jsonPath("$.schedules").isArray());
    }

    @DisplayName("Should return 400 invalid-arguments when flight number size is invalid on PATCH")
    @Test
    void shouldReturn400InvalidArguments_setRouteOperatingSchedules() throws Exception {
        AddRouteScheduleRequestDto requestDto = new AddRouteScheduleRequestDto(Set.of(LocalTime.of(10, 0)), Set.of(DayOfWeek.MONDAY));

        ResultActions response = mockMvc.perform(
                patch("/v1/routes/AV1/schedules")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto))
                        .accept(MediaType.APPLICATION_JSON));

        response.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").value("invalid-arguments"));
    }
}
