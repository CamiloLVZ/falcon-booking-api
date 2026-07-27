package com.falcon.booking.feature.airport.mapper;

import com.falcon.booking.feature.airport.dto.AirportDto;
import com.falcon.booking.feature.airport.dto.AirportSearchOptionDto;
import com.falcon.booking.persistence.entity.AirportEntity;
import com.falcon.booking.persistence.entity.CountryEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class AirportMapperTest {

    @InjectMocks
    private AirportMapperImpl airportMapper;

    private CountryEntity createCountry() {
        CountryEntity country = new CountryEntity();
        country.setId(1);
        country.setName("Colombia");
        country.setIsoCode("CO");
        return country;
    }

    private AirportEntity createAirportEntity() {
        AirportEntity entity = new AirportEntity();
        entity.setIataCode("BOG");
        entity.setName("El Dorado");
        entity.setCity("Bogota");
        entity.setCountry(createCountry());
        entity.setTimezone("America/Bogota");
        return entity;
    }

    @DisplayName("Should map airport entity to AirportDto")
    @Test
    void shouldMapToDto() {
        AirportEntity entity = createAirportEntity();

        AirportDto result = airportMapper.toDto(entity);

        assertThat(result.iataCode()).isEqualTo("BOG");
        assertThat(result.name()).isEqualTo("El Dorado");
        assertThat(result.city()).isEqualTo("Bogota");
        assertThat(result.country().name()).isEqualTo("Colombia");
        assertThat(result.country().isoCode()).isEqualTo("CO");
        assertThat(result.timezone()).isEqualTo("America/Bogota");
    }

    @DisplayName("Should map entity list to AirportDto list")
    @Test
    void shouldMapListToDto() {
        AirportEntity bog = createAirportEntity();
        AirportEntity mde = new AirportEntity();
        mde.setIataCode("MDE");
        mde.setName("Jose Maria Cordova");
        mde.setCity("Medellin");
        mde.setCountry(createCountry());
        mde.setTimezone("America/Bogota");

        List<AirportDto> result = airportMapper.toDto(List.of(bog, mde));

        assertThat(result).hasSize(2);
        assertThat(result.get(0).iataCode()).isEqualTo("BOG");
        assertThat(result.get(1).iataCode()).isEqualTo("MDE");
    }

    @DisplayName("Should map airport entity to AirportSearchOptionDto")
    @Test
    void shouldMapToSearchOptionDto() {
        AirportEntity entity = createAirportEntity();

        AirportSearchOptionDto result = airportMapper.toSearchOptionDto(entity);

        assertThat(result.iataCode()).isEqualTo("BOG");
        assertThat(result.city()).isEqualTo("Bogota");
        assertThat(result.name()).isEqualTo("El Dorado");
    }

    @DisplayName("Should map entity list to AirportSearchOptionDto list")
    @Test
    void shouldMapListToSearchOptionDto() {
        AirportEntity bog = createAirportEntity();
        AirportEntity mde = new AirportEntity();
        mde.setIataCode("MDE");
        mde.setName("Jose Maria Cordova");
        mde.setCity("Medellin");
        mde.setCountry(createCountry());
        mde.setTimezone("America/Bogota");

        List<AirportSearchOptionDto> result = airportMapper.toSearchOptionDto(List.of(bog, mde));

        assertThat(result).hasSize(2);
        assertThat(result.get(0).iataCode()).isEqualTo("BOG");
        assertThat(result.get(1).iataCode()).isEqualTo("MDE");
    }
}
