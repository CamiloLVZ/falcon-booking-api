package com.falcon.booking.web.controller;

import com.falcon.booking.domain.exception.CountryDoesNotExistException;
import com.falcon.booking.domain.service.AirportService;
import com.falcon.booking.domain.service.CountryService;
import com.falcon.booking.web.dto.CountryDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CountryController.class)
public class CountryControllerTest {

    @Autowired
    private MockMvc mockMvc;

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
                get("/countries/CO")
                .accept(MediaType.APPLICATION_JSON));

        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.isoCode", is(expectedDto.isoCode())))
                .andExpect(jsonPath("$.name", is(expectedDto.name())));
    }

    @DisplayName("Should return 404 not found when country is not found")
    @Test
    void shouldReturn404CountryNotFound_getCountry() throws Exception {
        given(countryService.getCountryByIsoCode("CO"))
                .willThrow( new CountryDoesNotExistException("CO"));

        ResultActions response = mockMvc.perform(
                get("/countries/CO")
                        .accept(MediaType.APPLICATION_JSON));

        response.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.type", is("country-does-not-exist")))
                .andExpect(jsonPath("$.message").exists());
    }

}
