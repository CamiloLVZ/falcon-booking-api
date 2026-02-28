package com.falcon.booking.web.controller;

import com.falcon.booking.domain.exception.Route.RouteNotFoundException;
import com.falcon.booking.domain.service.FlightService;
import com.falcon.booking.domain.service.RouteService;
import com.falcon.booking.domain.valueobject.FlightStatus;
import com.falcon.booking.domain.valueobject.RouteStatus;
import com.falcon.booking.web.dto.AirportDto;
import com.falcon.booking.web.dto.CountryDto;
import com.falcon.booking.web.dto.airplaneType.AirplaneTypeInFlightDto;
import com.falcon.booking.web.dto.airplaneType.ResponseAirplaneTypeDto;
import com.falcon.booking.web.dto.flight.ResponseFlightDto;
import com.falcon.booking.web.dto.flight.ResponseFlightsGeneratedDto;
import com.falcon.booking.web.dto.route.AddRouteScheduleRequestDto;
import com.falcon.booking.web.dto.route.CreateRouteDto;
import com.falcon.booking.web.dto.route.ResponseRouteDto;
import com.falcon.booking.web.dto.route.RouteWithSchedulesDto;
import com.falcon.booking.web.dto.route.UpdateRouteDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RouteController.class)
public class RouteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RouteService routeService;

    @MockitoBean
    private FlightService flightService;

    @Autowired
    private ObjectMapper objectMapper;

    private ResponseRouteDto createResponseRouteDto(String flightNumber) {
        CountryDto countryDto = new CountryDto("Colombia", "CO");
        AirportDto origin = new AirportDto("BOG", "El Dorado", "Bogota", countryDto, "America/Bogota");
        AirportDto destination = new AirportDto("MDE", "Jose Maria Cordoba", "Medellin", countryDto, "America/Bogota");
        ResponseAirplaneTypeDto airplaneType =
                new ResponseAirplaneTypeDto(1L, "Airbus", "A320", 100, 10, com.falcon.booking.domain.valueobject.AirplaneTypeStatus.ACTIVE);

        return new ResponseRouteDto(flightNumber, origin, destination, airplaneType, 60, RouteStatus.DRAFT);
    }

    @DisplayName("Should return 200 OK and route by flight number")
    @Test
    void shouldReturn200AndRoute_getRouteByFlightNumber() throws Exception {
        ResponseRouteDto responseDto = createResponseRouteDto("AV1234");
        given(routeService.getRouteByFlightNumber("AV1234")).willReturn(responseDto);

        ResultActions response = mockMvc.perform(get("/routes/AV1234").accept(MediaType.APPLICATION_JSON));

        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.flightNumber").value("AV1234"))
                .andExpect(jsonPath("$.airportOrigin.iataCode").value("BOG"));
    }

    @DisplayName("Should return 404 not found when route does not exist")
    @Test
    void shouldReturn404_getRouteByFlightNumber() throws Exception {
        given(routeService.getRouteByFlightNumber("AV1234")).willThrow(new RouteNotFoundException("AV1234"));

        ResultActions response = mockMvc.perform(get("/routes/AV1234").accept(MediaType.APPLICATION_JSON));

        response.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.type").value("route-does-not-exists"))
                .andExpect(jsonPath("$.message").exists());
    }

    @DisplayName("Should return 400 invalid-arguments when flight number size is invalid")
    @Test
    void shouldReturn400InvalidArguments_getRouteByFlightNumber() throws Exception {
        ResultActions response = mockMvc.perform(get("/routes/AV1").accept(MediaType.APPLICATION_JSON));

        response.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").value("invalid-arguments"));
    }

    @DisplayName("Should return 200 OK and route list")
    @Test
    void shouldReturn200AndRouteList_getAllRoutes() throws Exception {
        List<ResponseRouteDto> routes = List.of(createResponseRouteDto("AV1234"), createResponseRouteDto("AV5678"));
        given(routeService.getAllRoutes(null, null, null)).willReturn(routes);

        ResultActions response = mockMvc.perform(get("/routes").accept(MediaType.APPLICATION_JSON));

        response.andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.size()").value(2));
    }

    @DisplayName("Should return 201 created when route is added")
    @Test
    void shouldReturn201_addRoute() throws Exception {
        CreateRouteDto createRouteDto = new CreateRouteDto("AV1234", "BOG", "MDE", 1L, 60);
        ResponseRouteDto responseDto = createResponseRouteDto("AV1234");
        given(routeService.addRoute(createRouteDto)).willReturn(responseDto);

        ResultActions response = mockMvc.perform(
                post("/routes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRouteDto))
                        .accept(MediaType.APPLICATION_JSON)
        );

        response.andExpect(status().isCreated())
                .andExpect(jsonPath("$.flightNumber").value("AV1234"));
    }

    @DisplayName("Should return 400 bad request when create route body is invalid")
    @Test
    void shouldReturn400_addRoute() throws Exception {
        CreateRouteDto createRouteDto = new CreateRouteDto("", "BO", "", -1L, -1);

        ResultActions response = mockMvc.perform(
                post("/routes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRouteDto))
                        .accept(MediaType.APPLICATION_JSON)
        );

        response.andExpect(status().isBadRequest());
    }

    @DisplayName("Should return 200 OK when route is updated")
    @Test
    void shouldReturn200_updateRoute() throws Exception {
        UpdateRouteDto updateRouteDto = new UpdateRouteDto(null, null, null, 90);
        ResponseRouteDto responseDto = createResponseRouteDto("AV1234");
        given(routeService.updateRoute("AV1234", updateRouteDto)).willReturn(responseDto);

        ResultActions response = mockMvc.perform(
                put("/routes/AV1234")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRouteDto))
                        .accept(MediaType.APPLICATION_JSON)
        );

        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.flightNumber").value("AV1234"));
    }

    @DisplayName("Should return 200 OK when route is activated")
    @Test
    void shouldReturn200_activateRoute() throws Exception {
        ResponseRouteDto responseDto = createResponseRouteDto("AV1234");
        given(routeService.activateRoute("AV1234")).willReturn(responseDto);

        ResultActions response = mockMvc.perform(patch("/routes/AV1234/activate").accept(MediaType.APPLICATION_JSON));

        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.flightNumber").value("AV1234"));
    }

    @DisplayName("Should return 200 OK when route is deactivated")
    @Test
    void shouldReturn200_deactivateRoute() throws Exception {
        ResponseRouteDto responseDto = createResponseRouteDto("AV1234");
        given(routeService.deactivateRoute("AV1234")).willReturn(responseDto);

        ResultActions response = mockMvc.perform(patch("/routes/AV1234/deactivate").accept(MediaType.APPLICATION_JSON));

        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.flightNumber").value("AV1234"));
    }

    @DisplayName("Should return 200 OK when route schedules are set")
    @Test
    void shouldReturn200_setRouteOperatingSchedules() throws Exception {
        AddRouteScheduleRequestDto requestDto = new AddRouteScheduleRequestDto(
                Set.of(LocalTime.of(8, 0), LocalTime.of(10, 0)),
                Set.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY)
        );
        RouteWithSchedulesDto responseDto =
                new RouteWithSchedulesDto("AV1234", Set.of(DayOfWeek.MONDAY), Set.of(LocalTime.of(8, 0)));

        given(routeService.setRouteOperatingSchedules("AV1234", requestDto)).willReturn(responseDto);

        ResultActions response = mockMvc.perform(
                patch("/routes/AV1234/schedules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto))
                        .accept(MediaType.APPLICATION_JSON)
        );

        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.flightNumber").value("AV1234"));
    }

    @DisplayName("Should return 200 OK and route schedules")
    @Test
    void shouldReturn200_getRouteSchedules() throws Exception {
        RouteWithSchedulesDto responseDto =
                new RouteWithSchedulesDto("AV1234", Set.of(DayOfWeek.MONDAY), Set.of(LocalTime.of(8, 0)));
        given(routeService.getRouteWithSchedules("AV1234")).willReturn(responseDto);

        ResultActions response = mockMvc.perform(get("/routes/AV1234/schedules").accept(MediaType.APPLICATION_JSON));

        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.flightNumber").value("AV1234"));
    }

    @DisplayName("Should return 201 created when flights are generated for route")
    @Test
    void shouldReturn201_generateFlightsForRoute() throws Exception {
        ResponseFlightsGeneratedDto responseDto =
                new ResponseFlightsGeneratedDto("AV1234", 10, LocalDate.now(), LocalDate.now().plusDays(30));
        given(flightService.generateAllFlightsForRoute("AV1234")).willReturn(responseDto);

        ResultActions response = mockMvc.perform(post("/routes/AV1234/generateFlights").accept(MediaType.APPLICATION_JSON));

        response.andExpect(status().isCreated())
                .andExpect(jsonPath("$.flightNumber").value("AV1234"));
    }

    @DisplayName("Should return 200 OK for flights by route and dates")
    @Test
    void shouldReturn200_getAllFlightsByRouteAndDates() throws Exception {
        List<ResponseFlightDto> flights = List.of(
                new ResponseFlightDto(
                        1L,
                        "AV1234",
                        "BOG",
                        "MDE",
                        OffsetDateTime.now(),
                        LocalDateTime.now(),
                        new AirplaneTypeInFlightDto("Airbus", "A320", 100, 10),
                        FlightStatus.SCHEDULED
                )
        );
        given(flightService.getAllFlightsByRouteAndDates("AV1234", LocalDate.parse("2026-01-01"), LocalDate.parse("2026-01-02")))
                .willReturn(flights);

        ResultActions response = mockMvc.perform(
                get("/routes/AV1234/flights")
                        .param("dateFrom", "2026-01-01")
                        .param("dateTo", "2026-01-02")
                        .accept(MediaType.APPLICATION_JSON)
        );

        response.andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.size()").value(1));
    }
}
