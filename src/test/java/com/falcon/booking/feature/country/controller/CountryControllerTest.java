package com.falcon.booking.feature.country.controller;

import com.falcon.booking.feature.airport.dto.AirportDto;
import com.falcon.booking.feature.airport.service.AirportService;
import com.falcon.booking.feature.country.dto.CountryDto;
import com.falcon.booking.feature.country.exception.CountryNotFoundException;
import com.falcon.booking.feature.country.service.CountryService;
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
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WithMockUser(roles = "ADMIN")
@WebMvcTest(CountryController.class)
public class CountryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private CountryService countryService;

    @MockitoBean
    private AirportService airportService;

    @DisplayName("Should return 200 OK and CountryDto when country exists")
    @Test
    void shouldReturn200AndCountryDto_getCountry() throws Exception {
        CountryDto expectedDto = new CountryDto("Colombia","CO");
        given(countryService.getCountryByIsoCode("CO"))
                .willReturn(expectedDto);

        ResultActions response = mockMvc.perform(
                get("/v1/countries/CO")
                .accept(MediaType.APPLICATION_JSON));

        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.isoCode").value(expectedDto.isoCode()))
                .andExpect(jsonPath("$.name").value(expectedDto.name()));
    }

    @DisplayName("Should return 404 not found when country is not found")
    @Test
    void shouldReturn404CountryNotFound_getCountry() throws Exception {
        given(countryService.getCountryByIsoCode("CO"))
                .willThrow( new CountryNotFoundException("CO"));

        ResultActions response = mockMvc.perform(
                get("/v1/countries/CO")
                        .accept(MediaType.APPLICATION_JSON));

        response.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.type").value("country-does-not-exist"))
                .andExpect(jsonPath("$.message").exists());
    }

    @DisplayName("Should return 400 invalid-arguments when bad request to getCountry")
    @Test
    void shouldReturn400InvalidArgument_getCountry() throws Exception {
        String isoCode="COL";

        ResultActions response = mockMvc.perform(
                get("/v1/countries/{isoCode}", isoCode)
                        .accept(MediaType.APPLICATION_JSON));

        response.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").value("invalid-arguments"))
                .andExpect(jsonPath("$.message").exists());
    }

    @DisplayName("Should return 200 OK and list of countries")
    @Test
    void shouldReturn200AndCountryDtoList_getAllCountries() throws Exception {
        CountryDto country1 = new CountryDto("Colombia","CO");
        CountryDto country2 = new CountryDto("France","FR");
        CountryDto country3 = new CountryDto("Argentina","AR");
        List<CountryDto> countries = List.of(country1, country2, country3);
        given(countryService.getAllCountries()).willReturn(countries);

        ResultActions response = mockMvc.perform(get("/v1/countries")
                .accept(MediaType.APPLICATION_JSON));


        response.andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.size()").value(3))
                .andExpect(jsonPath("$[0].isoCode").value(country1.isoCode()))
                .andExpect(jsonPath("$[1].isoCode").value(country2.isoCode()))
                .andExpect(jsonPath("$[2].isoCode").value(country3.isoCode()));
    }

    @DisplayName("Should return 200 OK and empty list when there is no countries")
    @Test
    void shouldReturn200AndEmptyCountryDtoList_getAllCountries() throws Exception {
        List<CountryDto> countries = List.of();
        given(countryService.getAllCountries()).willReturn(countries);

        ResultActions response = mockMvc.perform(get("/v1/countries")
                .accept(MediaType.APPLICATION_JSON));


        response.andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.size()").value(0));
    }

    @DisplayName("Should return 200 OK and list of airport by country")
    @Test
    void shouldReturn200AndAirportDtoList_getAirportsByCountryIsoCode() throws Exception {
        CountryDto country1 = new CountryDto("Colombia","CO");
        AirportDto airport1 = new AirportDto("BOG", "El Dorado", "Bogota", country1,"America/Bogota");
        AirportDto airport2 = new AirportDto("MDE", "Jose Maria Cordoba", "Medellin", country1,"America/Bogota");
        Page<AirportDto> airports = new PageImpl<>(List.of(airport1, airport2), PageRequest.of(0, 10), 2);
        given(airportService.getAirportsByCountryIsoCode("CO", 0, 10)).willReturn(airports);

        ResultActions response = mockMvc.perform(get("/v1/countries/CO/airports")
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

    @DisplayName("Should return 200 OK and list empty of airport by country")
    @Test
    void shouldReturn200AndEmptyAirportDtoList_getAirportsByCountryIsoCode() throws Exception {
        Page<AirportDto> airports = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);
        given(airportService.getAirportsByCountryIsoCode("CO", 0, 10))
                .willReturn(airports);

        ResultActions response = mockMvc.perform(get("/v1/countries/CO/airports")
                .param("page", "0")
                .param("size", "10")
                .accept(MediaType.APPLICATION_JSON));


        response.andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.size()").value(0))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.totalPages").value(0))
                .andExpect(jsonPath("$.first").value(true))
                .andExpect(jsonPath("$.last").value(true));
    }

    @DisplayName("Should return 404 country not found in getAirportsByCountryIsoCode")
    @Test
    void shouldReturn404CountryNotFound_getAirportsByCountryIsoCode() throws Exception {
        given(airportService.getAirportsByCountryIsoCode("CO", 0, 10))
                .willThrow( new CountryNotFoundException("CO"));

        ResultActions response = mockMvc.perform(get("/v1/countries/CO/airports")
                .param("page", "0")
                .param("size", "10")
                .accept(MediaType.APPLICATION_JSON));


        response.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.type").value("country-does-not-exist"))
                .andExpect(jsonPath("$.message").exists());
    }


    @DisplayName("Should return 400 invalid-argument when bad request to getAirportsByCountryIsoCode")
    @Test
    void shouldReturn400InvalidArgument_getAirportsByCountryIsoCode() throws Exception {
        String isoCode="COL";

        ResultActions response = mockMvc.perform(
                get("/v1/countries/{isoCode}/airports", isoCode)
                        .param("page", "0")
                        .param("size", "10")
                        .accept(MediaType.APPLICATION_JSON));

        response.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").value("invalid-arguments"))
                .andExpect(jsonPath("$.message").exists());
    }

}




