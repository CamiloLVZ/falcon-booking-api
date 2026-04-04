package com.falcon.booking.web.controller;

import com.falcon.booking.domain.exception.FlightGeneration.FlightGenerationAlreadyRunningException;
import com.falcon.booking.domain.exception.Route.RouteNotFoundException;
import com.falcon.booking.domain.service.FlightService;
import com.falcon.booking.domain.service.RouteActivationOrchestrator;
import com.falcon.booking.domain.service.RouteService;
import com.falcon.booking.domain.valueobject.FlightGenerationStatus;
import com.falcon.booking.domain.valueobject.FlightGenerationType;
import com.falcon.booking.domain.valueobject.FlightStatus;
import com.falcon.booking.domain.valueobject.RouteStatus;
import com.falcon.booking.web.dto.AirportDto;
import com.falcon.booking.web.dto.CountryDto;
import com.falcon.booking.web.dto.airplaneType.AirplaneTypeInFlightDto;
import com.falcon.booking.web.dto.airplaneType.ResponseAirplaneTypeDto;
import com.falcon.booking.web.dto.flight.ResponseFlightDto;
import com.falcon.booking.web.dto.flight.ResponseFlightsGenerationDto;
import com.falcon.booking.web.dto.route.AddRouteScheduleRequestDto;
import com.falcon.booking.web.dto.route.CreateRouteDto;
import com.falcon.booking.web.dto.route.ResponseRouteDto;
import com.falcon.booking.web.dto.route.RouteWithSchedulesDto;
import com.falcon.booking.web.dto.route.UpdateRouteDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.falcon.booking.security.jwt.JwtUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.*;
import java.util.List;
import java.util.Set;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@WithMockUser(roles = "ADMIN")
@WebMvcTest(RouteController.class)
public class RouteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private RouteService routeService;

    @MockitoBean
    private FlightService flightService;

    @MockitoBean
    private RouteActivationOrchestrator routeActivationOrchestrator;

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

        ResultActions response = mockMvc.perform(get("/v1/routes/AV1234").accept(MediaType.APPLICATION_JSON));

        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.flightNumber").value("AV1234"))
                .andExpect(jsonPath("$.airportOrigin.iataCode").value("BOG"));
    }

    @DisplayName("Should return 404 not found when route does not exist")
    @Test
    void shouldReturn404_getRouteByFlightNumber() throws Exception {
        given(routeService.getRouteByFlightNumber("AV1234")).willThrow(new RouteNotFoundException("AV1234"));

        ResultActions response = mockMvc.perform(get("/v1/routes/AV1234").accept(MediaType.APPLICATION_JSON));

        response.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.type").value("route-does-not-exists"))
                .andExpect(jsonPath("$.message").exists());
    }

    @DisplayName("Should return 400 invalid-arguments when flight number size is invalid")
    @Test
    void shouldReturn400InvalidArguments_getRouteByFlightNumber() throws Exception {
        ResultActions response = mockMvc.perform(get("/v1/routes/AV1").accept(MediaType.APPLICATION_JSON));

        response.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").value("invalid-arguments"));
    }

    @DisplayName("Should return 200 OK and route list")
    @Test
    void shouldReturn200AndRouteList_getAllRoutes() throws Exception {
        List<ResponseRouteDto> routes = List.of(createResponseRouteDto("AV1234"), createResponseRouteDto("AV5678"));
        given(routeService.getAllRoutes(null, null, null)).willReturn(routes);

        ResultActions response = mockMvc.perform(get("/v1/routes").accept(MediaType.APPLICATION_JSON));

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
                post("/v1/routes")
                       .with(csrf())
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
                post("/v1/routes")
                       .with(csrf())
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
                put("/v1/routes/AV1234")
                       .with(csrf())
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
        given(routeActivationOrchestrator.activateRoute("AV1234")).willReturn(responseDto);

        ResultActions response = mockMvc.perform(
                patch("/v1/routes/AV1234/activate")
                        .with(csrf())
                        .accept(MediaType.APPLICATION_JSON));

        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.flightNumber").value("AV1234"));
    }

    @DisplayName("Should return 200 OK when route is deactivated")
    @Test
    void shouldReturn200_deactivateRoute() throws Exception {
        ResponseRouteDto responseDto = createResponseRouteDto("AV1234");
        given(routeService.deactivateRoute("AV1234")).willReturn(responseDto);

        ResultActions response = mockMvc.perform(
                patch("/v1/routes/AV1234/deactivate")
                        .with(csrf())
                        .accept(MediaType.APPLICATION_JSON));

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
                patch("/v1/routes/AV1234/schedules")
                       .with(csrf())
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

        ResultActions response = mockMvc.perform(get("/v1/routes/AV1234/schedules").accept(MediaType.APPLICATION_JSON));

        response.andExpect(status().isOk())
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
                get("/v1/routes/AV1234/flights")
                        .param("dateFrom", "2026-01-01")
                        .param("dateTo", "2026-01-02")
                        .accept(MediaType.APPLICATION_JSON)
        );

        response.andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.size()").value(1));
    }

    @DisplayName("Should return 202 when route flight generation starts")
    @Test
    void shouldReturn202_generateFlightsForRoute() throws Exception {
        ResponseFlightsGenerationDto dto = new ResponseFlightsGenerationDto(
                1L, FlightGenerationStatus.RUNNING, FlightGenerationType.ROUTE,
                1L, null, Instant.now(), null, null, "/flight-generations/1");
        given(flightService.startRouteFlightGeneration("AV1234")).willReturn(dto);


        ResultActions response = mockMvc.perform(
                post("/v1/routes/AV1234/generateFlights")
                        .with(csrf())
                        .accept(MediaType.APPLICATION_JSON));

        response.andExpect(status().isAccepted())
                .andExpect(jsonPath("$.generationId").value(1L))
                .andExpect(jsonPath("$.status").value("RUNNING"))
                .andExpect(jsonPath("$.type").value("ROUTE"));
    }

    @DisplayName("Should return 400 when a flight generation is already running")
    @Test
    void shouldReturn400GenerationAlwaysRunning_generateFlightsForRoute() throws Exception {
        given(flightService.startRouteFlightGeneration("AV1234"))
                .willThrow(new FlightGenerationAlreadyRunningException());

        ResultActions response = mockMvc.perform(
                post("/v1/routes/AV1234/generateFlights")
                        .with(csrf())
                        .accept(MediaType.APPLICATION_JSON));

        response.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").value("flight-generation-already-running"));
    }

    @DisplayName("Should return 202 when global flight generation starts")
    @Test
    void shouldReturn202_generateFlightsGlobal() throws Exception {
        ResponseFlightsGenerationDto dto = new ResponseFlightsGenerationDto(
                1L, FlightGenerationStatus.RUNNING, FlightGenerationType.GLOBAL,
                null, null, Instant.now(), null, null, "/flight-generations/1");
        given(flightService.startGlobalFlightGeneration()).willReturn(dto);

        ResultActions response = mockMvc.perform(
                post("/v1/routes/generateFlights")
                        .with(csrf())
                        .accept(MediaType.APPLICATION_JSON));

        response.andExpect(status().isAccepted())
                .andExpect(jsonPath("$.generationId").value(1L))
                .andExpect(jsonPath("$.status").value("RUNNING"))
                .andExpect(jsonPath("$.type").value("GLOBAL"));
    }

    @DisplayName("Should return 400 when a flight generation is already running")
    @Test
    void shouldReturn400GenerationAlwaysRunning_generateFlightsGlobal() throws Exception {
        given(flightService.startGlobalFlightGeneration())
                .willThrow(new FlightGenerationAlreadyRunningException());

        ResultActions response = mockMvc.perform(
                post("/v1/routes/generateFlights")
                        .with(csrf())
                        .accept(MediaType.APPLICATION_JSON));

        response.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").value("flight-generation-already-running"));
    }

}







