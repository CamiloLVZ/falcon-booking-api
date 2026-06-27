package com.falcon.booking.feature.airport.controller;

import com.falcon.booking.feature.airport.dto.AirportDto;
import com.falcon.booking.feature.airport.exception.AirportNotFoundException;
import com.falcon.booking.feature.airport.service.AirportService;
import com.falcon.booking.feature.country.dto.CountryDto;
import com.falcon.booking.security.jwt.JwtUtil;
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

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WithMockUser(roles = "ADMIN")
@WebMvcTest(AirportController.class)
public class AirportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private AirportService airportService;

    @DisplayName("Should return 200 OK and AirportDto when airport exists")
    @Test
    void shouldReturn200AndAirportDto_getAirport() throws Exception {
        CountryDto countryDto = new CountryDto("Colombia", "CO");
        AirportDto expectedDto = new AirportDto("BOG", "El Dorado", "Bogota", countryDto, "America/Bogota");
        given(airportService.getAirportByIataCode("BOG")).willReturn(expectedDto);

        ResultActions response = mockMvc.perform(
                get("/v1/airports/BOG").accept(MediaType.APPLICATION_JSON));

        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.iataCode").value(expectedDto.iataCode()))
                .andExpect(jsonPath("$.name").value(expectedDto.name()))
                .andExpect(jsonPath("$.city").value(expectedDto.city()))
                .andExpect(jsonPath("$.country.isoCode").value("CO"));
    }

    @DisplayName("Should return 404 not found when airport is not found")
    @Test
    void shouldReturn404AirportNotFound_getAirport() throws Exception {
        given(airportService.getAirportByIataCode("BOG")).willThrow(new AirportNotFoundException("BOG"));

        ResultActions response = mockMvc.perform(
                get("/v1/airports/BOG").accept(MediaType.APPLICATION_JSON));

        response.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.type").value("airport-does-not-exist"))
                .andExpect(jsonPath("$.message").exists());
    }

    @DisplayName("Should return 400 invalid-arguments when bad request to getAirport")
    @Test
    void shouldReturn400InvalidArgument_getAirport() throws Exception {
        String iataCode = "BOGG";

        ResultActions response = mockMvc.perform(
                get("/v1/airports/{iataCode}", iataCode).accept(MediaType.APPLICATION_JSON));

        response.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").value("invalid-arguments"))
                .andExpect(jsonPath("$.message").exists());
    }

    @DisplayName("Should return 200 OK and list of airports")
    @Test
    void shouldReturn200AndAirportDtoList_getAirports() throws Exception {
        CountryDto countryDto = new CountryDto("Colombia", "CO");
        AirportDto airport1 = new AirportDto("BOG", "El Dorado", "Bogota", countryDto, "America/Bogota");
        AirportDto airport2 = new AirportDto("MDE", "Jose Maria Cordoba", "Medellin", countryDto, "America/Bogota");
        Page<AirportDto> airports = new PageImpl<>(List.of(airport1, airport2), PageRequest.of(0, 10), 2);
        given(airportService.getAllAirports(0, 10)).willReturn(airports);

        ResultActions response = mockMvc.perform(
                get("/v1/airports")
                        .param("page", "0")
                        .param("size", "10")
                        .accept(MediaType.APPLICATION_JSON));

        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.size()").value(2))
                .andExpect(jsonPath("$.content[0].iataCode").value("BOG"))
                .andExpect(jsonPath("$.content[1].iataCode").value("MDE"))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.first").value(true))
                .andExpect(jsonPath("$.last").value(true));
    }

    @DisplayName("Should return 200 OK and empty list when there is no airports")
    @Test
    void shouldReturn200AndEmptyAirportDtoList_getAirports() throws Exception {
        Page<AirportDto> airports = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);
        given(airportService.getAllAirports(0, 10)).willReturn(airports);

        ResultActions response = mockMvc.perform(
                get("/v1/airports")
                        .param("page", "0")
                        .param("size", "10")
                        .accept(MediaType.APPLICATION_JSON));

        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.size()").value(0))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.totalPages").value(0))
                .andExpect(jsonPath("$.first").value(true))
                .andExpect(jsonPath("$.last").value(true));
    }
}




