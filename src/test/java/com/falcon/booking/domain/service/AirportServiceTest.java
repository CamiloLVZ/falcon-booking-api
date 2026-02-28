package com.falcon.booking.domain.service;

import com.falcon.booking.domain.exception.AirportNotFoundException;
import com.falcon.booking.domain.mapper.AirportMapper;
import com.falcon.booking.persistence.entity.AirportEntity;
import com.falcon.booking.persistence.entity.CountryEntity;
import com.falcon.booking.persistence.repository.AirportRepository;
import com.falcon.booking.web.dto.AirportDto;
import com.falcon.booking.web.dto.CountryDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class AirportServiceTest {

    @Mock
    private AirportRepository airportRepository;

    @Mock
    private AirportMapper airportMapper;

    @Mock
    private CountryService countryService;

    @InjectMocks
    private AirportService airportService;

    private CountryEntity createCountry(String isoCode, String name) {
        CountryEntity country = new CountryEntity();
        country.setIsoCode(isoCode);
        country.setName(name);
        return country;
    }

    private AirportEntity createAirport(String iataCode, String name, String city, CountryEntity country) {
        AirportEntity airport = new AirportEntity();
        airport.setIataCode(iataCode);
        airport.setName(name);
        airport.setCity(city);
        airport.setCountry(country);
        airport.setTimezone("America/Bogota");
        return airport;
    }

    @DisplayName("Should return AirportEntity when iata code exists")
    @Test
    void shouldReturnEntity_getAirportEntityByIataCode() {
        CountryEntity country = createCountry("CO", "Colombia");
        AirportEntity airport = createAirport("BOG", "El Dorado", "Bogota", country);
        given(airportRepository.findByIataCode("BOG"))
                .willReturn(Optional.of(airport));

        AirportEntity airportFound = airportService.getAirportEntityByIataCode(" bog ");

        assertThat(airportFound).isNotNull();
        assertThat(airportFound).isEqualTo(airport);
        verify(airportRepository).findByIataCode("BOG");
    }

    @DisplayName("Should throw exception when airport does not exist")
    @Test
    void shouldThrowException_getAirportEntityByIataCode() {
        given(airportRepository.findByIataCode("BOG"))
                .willReturn(Optional.empty());

        AirportNotFoundException ex = assertThrows(AirportNotFoundException.class,
                        () -> airportService.getAirportEntityByIataCode(" bog "));

        assertThat(ex.getMessage()).contains("bog");
        verify(airportRepository).findByIataCode("BOG");
    }

    @DisplayName("Should return AirportDto when airport exists")
    @Test
    void shouldReturnDto_getAirportByIataCode() {
        CountryEntity country = createCountry("CO", "Colombia");
        CountryDto countryDto = new CountryDto("Colombia", "CO");
        AirportEntity airport = createAirport("BOG", "El Dorado", "Bogota", country);
        AirportDto expectedDto = new AirportDto("BOG", "El Dorado", "Bogota", countryDto, "America/Bogota");
        given(airportRepository.findByIataCode("BOG")).willReturn(Optional.of(airport));
        given(airportMapper.toDto(airport)).willReturn(expectedDto);

        AirportDto airportFound = airportService.getAirportByIataCode("bog ");

        verify(airportRepository).findByIataCode("BOG");
        verify(airportMapper).toDto(airport);
        assertThat(airportFound).isEqualTo(expectedDto);
    }

    @DisplayName("Should throw exception when airport does not exist in getAirportByIataCode")
    @Test
    void shouldThrowException_getAirportByIataCode() {
        given(airportRepository.findByIataCode("MDE")).willReturn(Optional.empty());

        AirportNotFoundException ex = assertThrows(AirportNotFoundException.class,
                        () -> airportService.getAirportByIataCode("mde"));

        assertThat(ex.getMessage()).contains("mde");
        verify(airportRepository).findByIataCode("MDE");
    }

    @DisplayName("Should return AirportDto list when getAllAirports is called")
    @Test
    void shouldReturnDtoList_getAllAirports() {
        CountryEntity country = createCountry("CO", "Colombia");
        CountryDto countryDto = new CountryDto("Colombia", "CO");
        AirportEntity airport1 = createAirport("BOG", "El Dorado", "Bogota", country);
        AirportEntity airport2 = createAirport("MDE", "Jose Maria Cordoba", "Medellin", country);
        List<AirportEntity> airportList = List.of(airport1, airport2);
        AirportDto dto1 = new AirportDto("BOG", "El Dorado", "Bogota", countryDto, "America/Bogota");
        AirportDto dto2 = new AirportDto("MDE", "Jose Maria Cordoba", "Medellin", countryDto, "America/Bogota");
        List<AirportDto> expectedDtoList = List.of(dto1, dto2);
        given(airportRepository.findAll()).willReturn(airportList);
        given(airportMapper.toDto(airportList)).willReturn(expectedDtoList);

        List<AirportDto> listFound = airportService.getAllAirports();

        verify(airportRepository).findAll();
        verify(airportMapper).toDto(airportList);
        assertThat(listFound).isEqualTo(expectedDtoList);
    }

    @DisplayName("Should return empty AirportDto list when there is no airports")
    @Test
    void shouldReturnEmptyDtoList_getAllAirports() {
        given(airportRepository.findAll()).willReturn(List.of());
        given(airportMapper.toDto(List.of())).willReturn(List.of());

        List<AirportDto> listFound = airportService.getAllAirports();

        verify(airportRepository).findAll();
        verify(airportMapper).toDto(List.of());
        assertThat(listFound).isNotNull();
        assertThat(listFound).isEmpty();
    }

    @DisplayName("Should return AirportDto list by country iso code")
    @Test
    void shouldReturnDtoList_getAirportsByCountryIsoCode() {
        CountryEntity country = createCountry("CO", "Colombia");
        CountryDto countryDto = new CountryDto("Colombia", "CO");
        AirportEntity airport1 = createAirport("BOG", "El Dorado", "Bogota", country);
        AirportEntity airport2 = createAirport("MDE", "Jose Maria Cordoba", "Medellin", country);
        List<AirportEntity> airportEntities = List.of(airport1, airport2);
        AirportDto dto1 = new AirportDto("BOG", "El Dorado", "Bogota", countryDto, "America/Bogota");
        AirportDto dto2 = new AirportDto("MDE", "Jose Maria Cordoba", "Medellin", countryDto, "America/Bogota");
        List<AirportDto> expectedDtos = List.of(dto1, dto2);
        given(countryService.getCountryEntityByIsoCode(" CO ")).willReturn(country);
        given(airportRepository.findAllByCountry(country)).willReturn(airportEntities);
        given(airportMapper.toDto(airportEntities)).willReturn(expectedDtos);

        List<AirportDto> result = airportService.getAirportsByCountryIsoCode(" CO ");

        verify(countryService).getCountryEntityByIsoCode(" CO ");
        verify(airportRepository).findAllByCountry(country);
        verify(airportMapper).toDto(airportEntities);
        assertThat(result).isEqualTo(expectedDtos);
    }

    @DisplayName("Should return empty AirportDto list when country has no airports")
    @Test
    void shouldReturnEmptyDtoList_getAirportsByCountryIsoCode() {
        CountryEntity country = createCountry("CO", "Colombia");
        given(countryService.getCountryEntityByIsoCode("CO")).willReturn(country);
        given(airportRepository.findAllByCountry(country)).willReturn(List.of());
        given(airportMapper.toDto(List.of())).willReturn(List.of());

        List<AirportDto> result = airportService.getAirportsByCountryIsoCode("CO");

        verify(countryService).getCountryEntityByIsoCode("CO");
        verify(airportRepository).findAllByCountry(country);
        verify(airportMapper).toDto(List.of());
        assertThat(result).isEmpty();
    }
}
