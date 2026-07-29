package com.falcon.booking.feature.catalog.controller;

import com.falcon.booking.feature.airplaneType.dto.AirplaneTypeOptionDto;
import com.falcon.booking.feature.airport.dto.AirportSearchOptionDto;
import com.falcon.booking.feature.catalog.dto.CatalogDropdownDto;
import com.falcon.booking.feature.catalog.service.CatalogService;
import com.falcon.booking.feature.country.dto.CountryDto;
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

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CatalogController.class)
class CatalogControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private CatalogService catalogService;

    @DisplayName("Should return 200 OK with dropdown options for admin user")
    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldReturnDropdownOptionsForAdmin() throws Exception {
        CatalogDropdownDto dto = new CatalogDropdownDto(
                List.of(new AirportSearchOptionDto("BOG", "Bogota", "El Dorado")),
                List.of(new AirplaneTypeOptionDto(1L, "Boeing", "737"), new AirplaneTypeOptionDto(2L, "Airbus", "A320")),
                List.of(new CountryDto("Colombia", "CO"))
        );
        given(catalogService.getDropdownOptions(true)).willReturn(dto);

        ResultActions response = mockMvc.perform(get("/v1/catalog/dropdown-options")
                .accept(MediaType.APPLICATION_JSON));

        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.airports").isArray())
                .andExpect(jsonPath("$.airports[0].iataCode").value("BOG"))
                .andExpect(jsonPath("$.airplaneTypes").isArray())
                .andExpect(jsonPath("$.airplaneTypes[0].id").value(1))
                .andExpect(jsonPath("$.airplaneTypes[0].producer").value("Boeing"))
                .andExpect(jsonPath("$.airplaneTypes[0].model").value("737"))
                .andExpect(jsonPath("$.countries").isArray())
                .andExpect(jsonPath("$.countries[0].isoCode").value("CO"))
                .andExpect(jsonPath("$.countries[0].name").value("Colombia"));
    }

    @DisplayName("Should return 200 OK with dropdown options for non-admin user (only ACTIVE types)")
    @Test
    @WithMockUser(roles = "CLIENT")
    void shouldReturnDropdownOptionsForNonAdmin() throws Exception {
        CatalogDropdownDto dto = new CatalogDropdownDto(
                List.of(new AirportSearchOptionDto("CTG", "Cartagena", "Rafael Nunez")),
                List.of(new AirplaneTypeOptionDto(1L, "Boeing", "737")),
                List.of(new CountryDto("Colombia", "CO"))
        );
        given(catalogService.getDropdownOptions(false)).willReturn(dto);

        ResultActions response = mockMvc.perform(get("/v1/catalog/dropdown-options")
                .accept(MediaType.APPLICATION_JSON));

        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.airports[0].iataCode").value("CTG"))
                .andExpect(jsonPath("$.airplaneTypes").isArray())
                .andExpect(jsonPath("$.airplaneTypes.length()").value(1));
    }

    @DisplayName("Should return 200 OK and origin airport list")
    @Test
    @WithMockUser(roles = "CLIENT")
    void shouldReturnOriginAirports() throws Exception {
        List<AirportSearchOptionDto> airports = List.of(
                new AirportSearchOptionDto("BOG", "Bogota", "El Dorado"),
                new AirportSearchOptionDto("MDE", "Medellin", "Jose Maria Cordoba")
        );
        given(catalogService.getOriginAirports()).willReturn(airports);

        ResultActions response = mockMvc.perform(get("/v1/catalog/origin-airports")
                .accept(MediaType.APPLICATION_JSON));

        response.andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.size()").value(2))
                .andExpect(jsonPath("$[0].iataCode").value("BOG"));
    }

    @DisplayName("Should return 200 OK and destination airport list by origin")
    @Test
    @WithMockUser(roles = "CLIENT")
    void shouldReturnDestinationAirports() throws Exception {
        List<AirportSearchOptionDto> airports = List.of(
                new AirportSearchOptionDto("MDE", "Medellin", "Jose Maria Cordoba"),
                new AirportSearchOptionDto("CLO", "Cali", "Alfonso Bonilla Aragon")
        );
        given(catalogService.getDestinationAirports("BOG")).willReturn(airports);

        ResultActions response = mockMvc.perform(
                get("/v1/catalog/destination-airports")
                        .param("originIataCode", "BOG")
                        .accept(MediaType.APPLICATION_JSON));

        response.andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.size()").value(2))
                .andExpect(jsonPath("$[0].iataCode").value("MDE"));
    }

    @DisplayName("Should return 400 when origin IATA code size is invalid")
    @Test
    @WithMockUser(roles = "CLIENT")
    void shouldReturn400InvalidArguments_getDestinationAirports() throws Exception {
        ResultActions response = mockMvc.perform(
                get("/v1/catalog/destination-airports")
                        .param("originIataCode", "BO")
                        .accept(MediaType.APPLICATION_JSON));

        response.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").value("invalid-arguments"));
    }
}