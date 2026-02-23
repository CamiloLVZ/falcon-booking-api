package com.falcon.booking.web.controller;

import com.falcon.booking.domain.exception.AirportNotFoundException;
import com.falcon.booking.domain.service.AirportService;
import com.falcon.booking.web.dto.AirportDto;
import com.falcon.booking.web.dto.CountryDto;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AirportController.class)
public class AirportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AirportService airportService;

    @DisplayName("Should return 200 OK and AirportDto when airport exists")
    @Test
    void shouldReturn200AndAirportDto_getAirport() throws Exception {
        CountryDto countryDto = new CountryDto("Colombia", "CO");
        AirportDto expectedDto = new AirportDto("BOG", "El Dorado", "Bogota", countryDto, "America/Bogota");
        given(airportService.getAirportByIataCode("BOG")).willReturn(expectedDto);

        ResultActions response = mockMvc.perform(
                get("/airports/BOG").accept(MediaType.APPLICATION_JSON));

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
                get("/airports/BOG").accept(MediaType.APPLICATION_JSON));

        response.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.type").value("airport-does-not-exist"))
                .andExpect(jsonPath("$.message").exists());
    }

    @DisplayName("Should return 400 invalid-arguments when bad request to getAirport")
    @Test
    void shouldReturn400InvalidArgument_getAirport() throws Exception {
        String iataCode = "BOGG";

        ResultActions response = mockMvc.perform(
                get("/airports/{iataCode}", iataCode).accept(MediaType.APPLICATION_JSON));

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
        List<AirportDto> airports = List.of(airport1, airport2);
        given(airportService.getAllAirports()).willReturn(airports);

        ResultActions response = mockMvc.perform(
                get("/airports").accept(MediaType.APPLICATION_JSON));

        response.andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.size()").value(2))
                .andExpect(jsonPath("$[0].iataCode").value("BOG"))
                .andExpect(jsonPath("$[1].iataCode").value("MDE"));
    }

    @DisplayName("Should return 200 OK and empty list when there is no airports")
    @Test
    void shouldReturn200AndEmptyAirportDtoList_getAirports() throws Exception {
        List<AirportDto> airports = List.of();
        given(airportService.getAllAirports()).willReturn(airports);

        ResultActions response = mockMvc.perform(
                get("/airports").accept(MediaType.APPLICATION_JSON));

        response.andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.size()").value(0));
    }
}
