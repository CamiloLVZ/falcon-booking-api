package com.falcon.booking.feature.airport.service;

import com.falcon.booking.feature.airport.dto.AirportDto;
import com.falcon.booking.feature.airport.exception.AirportNotFoundException;
import com.falcon.booking.feature.airport.mapper.AirportMapper;
import com.falcon.booking.feature.country.dto.CountryDto;
import com.falcon.booking.feature.country.service.CountryService;
import com.falcon.booking.persistence.entity.AirportEntity;
import com.falcon.booking.persistence.entity.CountryEntity;
import com.falcon.booking.persistence.repository.AirportRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
        Pageable pageable = PageRequest.of(0, 10, Sort.by("city").ascending());
        Page<AirportEntity> airportPage = new PageImpl<>(List.of(airport1, airport2), pageable, 2);
        AirportDto dto1 = new AirportDto("BOG", "El Dorado", "Bogota", countryDto, "America/Bogota");
        AirportDto dto2 = new AirportDto("MDE", "Jose Maria Cordoba", "Medellin", countryDto, "America/Bogota");
        given(airportRepository.findAll(any(Specification.class), eq(pageable))).willReturn(airportPage);
        given(airportMapper.toDto(airport1)).willReturn(dto1);
        given(airportMapper.toDto(airport2)).willReturn(dto2);

        Page<AirportDto> pageFound = airportService.getAllAirports(null, null, 0, 10);

        verify(airportRepository).findAll(any(Specification.class), eq(pageable));
        verify(airportMapper).toDto(airport1);
        verify(airportMapper).toDto(airport2);
        assertThat(pageFound.getContent()).containsExactly(dto1, dto2);
        assertThat(pageFound.getNumber()).isZero();
        assertThat(pageFound.getSize()).isEqualTo(10);
        assertThat(pageFound.getTotalElements()).isEqualTo(2);
        assertThat(pageFound.getTotalPages()).isEqualTo(1);
    }

    @DisplayName("Should return empty AirportDto list when there is no airports")
    @Test
    void shouldReturnEmptyDtoList_getAllAirports() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("city").ascending());
        Page<AirportEntity> airportPage = new PageImpl<>(List.of(), pageable, 0);
        given(airportRepository.findAll(any(Specification.class), eq(pageable))).willReturn(airportPage);

        Page<AirportDto> pageFound = airportService.getAllAirports(null, null, 0, 10);

        verify(airportRepository).findAll(any(Specification.class), eq(pageable));
        assertThat(pageFound).isNotNull();
        assertThat(pageFound.getContent()).isEmpty();
        assertThat(pageFound.getNumber()).isZero();
        assertThat(pageFound.getSize()).isEqualTo(10);
        assertThat(pageFound.getTotalElements()).isZero();
        assertThat(pageFound.getTotalPages()).isZero();
    }

    @DisplayName("Should filter airports by country ISO code")
    @Test
    void shouldFilterAirportsByCountry() {
        CountryEntity country = createCountry("CO", "Colombia");
        CountryDto countryDto = new CountryDto("Colombia", "CO");
        AirportEntity airport = createAirport("BOG", "El Dorado", "Bogota", country);
        Pageable pageable = PageRequest.of(0, 10, Sort.by("city").ascending());
        Page<AirportEntity> airportPage = new PageImpl<>(List.of(airport), pageable, 1);
        AirportDto dto = new AirportDto("BOG", "El Dorado", "Bogota", countryDto, "America/Bogota");
        given(airportRepository.findAll(any(Specification.class), eq(pageable))).willReturn(airportPage);
        given(airportMapper.toDto(airport)).willReturn(dto);

        Page<AirportDto> result = airportService.getAllAirports("CO", null, 0, 10);

        assertThat(result.getContent()).containsExactly(dto);
    }

    @DisplayName("Should filter airports by search term")
    @Test
    void shouldFilterAirportsBySearch() {
        CountryEntity country = createCountry("CO", "Colombia");
        CountryDto countryDto = new CountryDto("Colombia", "CO");
        AirportEntity airport = createAirport("BOG", "El Dorado", "Bogota", country);
        Pageable pageable = PageRequest.of(0, 10, Sort.by("city").ascending());
        Page<AirportEntity> airportPage = new PageImpl<>(List.of(airport), pageable, 1);
        AirportDto dto = new AirportDto("BOG", "El Dorado", "Bogota", countryDto, "America/Bogota");
        given(airportRepository.findAll(any(Specification.class), eq(pageable))).willReturn(airportPage);
        given(airportMapper.toDto(airport)).willReturn(dto);

        Page<AirportDto> result = airportService.getAllAirports(null, "Dorado", 0, 10);

        assertThat(result.getContent()).containsExactly(dto);
    }

    @DisplayName("Should filter airports by both country and search")
    @Test
    void shouldFilterAirportsByCountryAndSearch() {
        CountryEntity country = createCountry("CO", "Colombia");
        CountryDto countryDto = new CountryDto("Colombia", "CO");
        AirportEntity airport = createAirport("BOG", "El Dorado", "Bogota", country);
        Pageable pageable = PageRequest.of(0, 10, Sort.by("city").ascending());
        Page<AirportEntity> airportPage = new PageImpl<>(List.of(airport), pageable, 1);
        AirportDto dto = new AirportDto("BOG", "El Dorado", "Bogota", countryDto, "America/Bogota");
        given(airportRepository.findAll(any(Specification.class), eq(pageable))).willReturn(airportPage);
        given(airportMapper.toDto(airport)).willReturn(dto);

        Page<AirportDto> result = airportService.getAllAirports("CO", "Dorado", 0, 10);

        assertThat(result.getContent()).containsExactly(dto);
    }

    @DisplayName("Should return AirportDto list by country iso code")
    @Test
    void shouldReturnDtoList_getAirportsByCountryIsoCode() {
        CountryEntity country = createCountry("CO", "Colombia");
        CountryDto countryDto = new CountryDto("Colombia", "CO");
        AirportEntity airport1 = createAirport("BOG", "El Dorado", "Bogota", country);
        AirportEntity airport2 = createAirport("MDE", "Jose Maria Cordoba", "Medellin", country);
        Pageable pageable = PageRequest.of(0, 10, Sort.by("city").ascending());
        Page<AirportEntity> airportPage = new PageImpl<>(List.of(airport1, airport2), pageable, 2);
        AirportDto dto1 = new AirportDto("BOG", "El Dorado", "Bogota", countryDto, "America/Bogota");
        AirportDto dto2 = new AirportDto("MDE", "Jose Maria Cordoba", "Medellin", countryDto, "America/Bogota");
        given(countryService.getCountryEntityByIsoCode(" CO ")).willReturn(country);
        given(airportRepository.findAllByCountry(country, pageable)).willReturn(airportPage);
        given(airportMapper.toDto(airport1)).willReturn(dto1);
        given(airportMapper.toDto(airport2)).willReturn(dto2);

        Page<AirportDto> result = airportService.getAirportsByCountryIsoCode(" CO ", 0, 10);

        verify(countryService).getCountryEntityByIsoCode(" CO ");
        verify(airportRepository).findAllByCountry(country, pageable);
        verify(airportMapper).toDto(airport1);
        verify(airportMapper).toDto(airport2);
        assertThat(result.getContent()).containsExactly(dto1, dto2);
        assertThat(result.getNumber()).isZero();
        assertThat(result.getSize()).isEqualTo(10);
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getTotalPages()).isEqualTo(1);
    }

    @DisplayName("Should return empty AirportDto list when country has no airports")
    @Test
    void shouldReturnEmptyDtoList_getAirportsByCountryIsoCode() {
        CountryEntity country = createCountry("CO", "Colombia");
        Pageable pageable = PageRequest.of(0, 10, Sort.by("city").ascending());
        Page<AirportEntity> airportPage = new PageImpl<>(List.of(), pageable, 0);
        given(countryService.getCountryEntityByIsoCode("CO")).willReturn(country);
        given(airportRepository.findAllByCountry(country, pageable)).willReturn(airportPage);

        Page<AirportDto> result = airportService.getAirportsByCountryIsoCode("CO", 0, 10);

        verify(countryService).getCountryEntityByIsoCode("CO");
        verify(airportRepository).findAllByCountry(country, pageable);
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getNumber()).isZero();
        assertThat(result.getSize()).isEqualTo(10);
        assertThat(result.getTotalElements()).isZero();
        assertThat(result.getTotalPages()).isZero();
    }
}
