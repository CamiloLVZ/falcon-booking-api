package com.falcon.booking.feature.route.controller;

import com.falcon.booking.common.enums.*;
import com.falcon.booking.feature.airplaneType.dto.AirplaneTypeInFlightDto;
import com.falcon.booking.feature.airplaneType.dto.ResponseAirplaneTypeDto;
import com.falcon.booking.feature.airport.dto.AirportDto;
import com.falcon.booking.feature.airport.dto.AirportSearchOptionDto;
import com.falcon.booking.feature.country.dto.CountryDto;
import com.falcon.booking.feature.flight.dto.ResponseFlightDto;
import com.falcon.booking.feature.flight.service.FlightQueryService;
import com.falcon.booking.feature.flightGeneration.dto.ResponseFlightsGenerationDto;
import com.falcon.booking.feature.flightGeneration.exception.FlightGenerationAlreadyRunningException;
import com.falcon.booking.feature.flightGeneration.service.FlightGenerationService;
import com.falcon.booking.feature.route.dto.CreateRouteDto;
import com.falcon.booking.feature.route.dto.ResponseRouteDto;
import com.falcon.booking.feature.route.dto.UpdateRouteDto;
import com.falcon.booking.feature.route.exception.RouteNotFoundException;
import com.falcon.booking.feature.route.service.RouteActivationOrchestrator;
import com.falcon.booking.feature.route.service.RouteCommandService;
import com.falcon.booking.feature.route.service.RouteQueryService;
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

import java.math.BigDecimal;
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
@WebMvcTest(RouteController.class)
public class RouteControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private RouteQueryService routeQueryService;

    @MockitoBean
    private RouteCommandService routeCommandService;


    @MockitoBean
    private FlightQueryService flightQueryService;

    @MockitoBean
    private RouteActivationOrchestrator routeActivationOrchestrator;

    @MockitoBean
    private FlightGenerationService flightGenerationService;

    @Autowired
    private ObjectMapper objectMapper;

    private ResponseRouteDto createResponseRouteDto(String flightNumber) {
        CountryDto countryDto = new CountryDto("Colombia", "CO");
        AirportDto origin = new AirportDto("BOG", "El Dorado", "Bogota", countryDto, "America/Bogota");
        AirportDto destination = new AirportDto("MDE", "Jose Maria Cordoba", "Medellin", countryDto, "America/Bogota");
        ResponseAirplaneTypeDto airplaneType =
                new ResponseAirplaneTypeDto(1L, "Airbus", "A320", 100, 10, "ABCDEF", AirplaneTypeStatus.ACTIVE);

        return new ResponseRouteDto(flightNumber, origin, destination, airplaneType, 60, RouteStatus.DRAFT, BigDecimal.valueOf(100.0), BigDecimal.valueOf(200.0));
    }

    @DisplayName("Should return 200 OK and route by flight number")
    @Test
    void shouldReturn200AndRoute_getRouteByFlightNumber() throws Exception {
        ResponseRouteDto responseDto = createResponseRouteDto("AV1234");
        given(routeQueryService.getRouteByFlightNumber("AV1234")).willReturn(responseDto);

        ResultActions response = mockMvc.perform(get("/v1/routes/AV1234").accept(MediaType.APPLICATION_JSON));

        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.flightNumber").value("AV1234"))
                .andExpect(jsonPath("$.airportOrigin.iataCode").value("BOG"));
    }

    @DisplayName("Should return 404 not found when route does not exist")
    @Test
    void shouldReturn404_getRouteByFlightNumber() throws Exception {
        given(routeQueryService.getRouteByFlightNumber("AV1234")).willThrow(new RouteNotFoundException("AV1234"));

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
        Page<ResponseRouteDto> routes = new PageImpl<>(
                List.of(createResponseRouteDto("AV1234"), createResponseRouteDto("AV5678")),
                PageRequest.of(0, 10),
                2);
        given(routeQueryService.getAllRoutes(null, null, null, 0, 10)).willReturn(routes);

        ResultActions response = mockMvc.perform(get("/v1/routes")
                .param("page", "0")
                .param("size", "10")
                .accept(MediaType.APPLICATION_JSON));

        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.size()").value(2))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @DisplayName("Should return 200 OK and origin airport list")
    @Test
    void shouldReturn200AndOriginAirportList_getOriginAirports() throws Exception {
        List<AirportSearchOptionDto> airports = List.of(
                new AirportSearchOptionDto("BOG", "Bogota", "El Dorado"),
                new AirportSearchOptionDto("MDE", "Medellin", "Jose Maria Cordoba")
        );
        given(routeQueryService.getOriginAirports()).willReturn(airports);

        ResultActions response = mockMvc.perform(get("/v1/routes/search/origins").accept(MediaType.APPLICATION_JSON));

        response.andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.size()").value(2))
                .andExpect(jsonPath("$[0].iataCode").value("BOG"));
    }

    @DisplayName("Should return 200 OK and destination airport list by origin")
    @Test
    void shouldReturn200AndDestinationAirportList_getDestinationAirports() throws Exception {
        List<AirportSearchOptionDto> airports = List.of(
                new AirportSearchOptionDto("MDE", "Medellin", "Jose Maria Cordoba"),
                new AirportSearchOptionDto("CLO", "Cali", "Alfonso Bonilla Aragon")
        );
        given(routeQueryService.getDestinationAirports("BOG")).willReturn(airports);

        ResultActions response = mockMvc.perform(
                get("/v1/routes/search/destinations")
                        .param("originIataCode", "BOG")
                        .accept(MediaType.APPLICATION_JSON));

        response.andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.size()").value(2))
                .andExpect(jsonPath("$[0].iataCode").value("MDE"));
    }

    @DisplayName("Should return 400 invalid-arguments when origin IATA code size is invalid")
    @Test
    void shouldReturn400InvalidArguments_getDestinationAirports() throws Exception {
        ResultActions response = mockMvc.perform(
                get("/v1/routes/search/destinations")
                        .param("originIataCode", "BO")
                        .accept(MediaType.APPLICATION_JSON));

        response.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").value("invalid-arguments"));
    }

    @DisplayName("Should return 201 created when route is added")
    @Test
    void shouldReturn201_addRoute() throws Exception {
        CreateRouteDto createRouteDto = new CreateRouteDto("AV1234", "BOG", "MDE", 1L, 60, BigDecimal.valueOf(100.0), BigDecimal.valueOf(200.0));
        ResponseRouteDto responseDto = createResponseRouteDto("AV1234");
        given(routeCommandService.addRoute(createRouteDto)).willReturn(responseDto);

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
        CreateRouteDto createRouteDto = new CreateRouteDto("", "BO", "", -1L, -1, BigDecimal.valueOf(100.0), BigDecimal.valueOf(200.0));

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
        UpdateRouteDto updateRouteDto = new UpdateRouteDto(null, null, null, 90, null, null);
        ResponseRouteDto responseDto = createResponseRouteDto("AV1234");
        given(routeCommandService.updateRoute("AV1234", updateRouteDto)).willReturn(responseDto);

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
        given(routeCommandService.deactivateRoute("AV1234")).willReturn(responseDto);

        ResultActions response = mockMvc.perform(
                patch("/v1/routes/AV1234/deactivate")
                        .with(csrf())
                        .accept(MediaType.APPLICATION_JSON));

        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.flightNumber").value("AV1234"));
    }


    @DisplayName("Should return 200 OK for flights by route and dates")
    @Test
    void shouldReturn200_getAllFlightsByRouteAndDates() throws Exception {
        Page<ResponseFlightDto> flights = new PageImpl<>(List.of(
                new ResponseFlightDto(
                        1L,
                        "AV1234",
                        "BOG",
                        "MDE",
                        OffsetDateTime.now(),
                        LocalDateTime.now(),
                        40,
                        new AirplaneTypeInFlightDto("Airbus", "A320", 100, 10, "ABCDEF"),
                        FlightStatus.SCHEDULED,
                        BigDecimal.valueOf(100.0),
                        BigDecimal.valueOf(200.0)
                )
        ), PageRequest.of(0, 10), 1);
        given(flightQueryService.getAllFlightsByRouteAndDate("AV1234", LocalDate.parse("2026-01-01"), 0, 10))
                .willReturn(flights);

        ResultActions response = mockMvc.perform(
                get("/v1/routes/AV1234/flights")
                        .param("date", "2026-01-01")
                        .param("page", "0")
                        .param("size", "10")
                        .accept(MediaType.APPLICATION_JSON)
        );

        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.size()").value(1))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @DisplayName("Should return 202 when route flight generation starts")
    @Test
    void shouldReturn202_generateFlightsForRoute() throws Exception {
        ResponseFlightsGenerationDto dto = new ResponseFlightsGenerationDto(
                1L, FlightGenerationStatus.RUNNING, FlightGenerationType.ROUTE,
                1L, null, Instant.now(), null, null, "/flight-generations/1");
        given(flightGenerationService.startRouteFlightGeneration("AV1234")).willReturn(dto);


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
        given(flightGenerationService.startRouteFlightGeneration("AV1234"))
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
        given(flightGenerationService.startGlobalFlightGeneration()).willReturn(dto);

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
        given(flightGenerationService.startGlobalFlightGeneration())
                .willThrow(new FlightGenerationAlreadyRunningException());

        ResultActions response = mockMvc.perform(
                post("/v1/routes/generateFlights")
                        .with(csrf())
                        .accept(MediaType.APPLICATION_JSON));

        response.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").value("flight-generation-already-running"));
    }

}







