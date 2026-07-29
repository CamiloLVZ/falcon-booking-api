package com.falcon.booking.feature.catalog.service;

import com.falcon.booking.common.enums.AirplaneTypeStatus;
import com.falcon.booking.feature.airplaneType.dto.AirplaneTypeOptionDto;
import com.falcon.booking.feature.airplaneType.mapper.AirplaneTypeMapper;
import com.falcon.booking.feature.airport.dto.AirportSearchOptionDto;
import com.falcon.booking.feature.airport.mapper.AirportMapper;
import com.falcon.booking.feature.catalog.dto.CatalogDropdownDto;
import com.falcon.booking.feature.country.dto.CountryDto;
import com.falcon.booking.feature.country.mapper.CountryMapper;
import com.falcon.booking.feature.route.service.RouteQueryService;
import com.falcon.booking.persistence.entity.AirplaneTypeEntity;
import com.falcon.booking.persistence.entity.AirportEntity;
import com.falcon.booking.persistence.entity.CountryEntity;
import com.falcon.booking.persistence.repository.AirplaneTypeRepository;
import com.falcon.booking.persistence.repository.AirportRepository;
import com.falcon.booking.persistence.repository.CountryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CatalogServiceTest {

    @Mock
    private AirportRepository airportRepository;
    @Mock
    private AirplaneTypeRepository airplaneTypeRepository;
    @Mock
    private CountryRepository countryRepository;
    @Mock
    private AirportMapper airportMapper;
    @Mock
    private AirplaneTypeMapper airplaneTypeMapper;
    @Mock
    private CountryMapper countryMapper;
    @Mock
    private RouteQueryService routeQueryService;

    @InjectMocks
    private CatalogService catalogService;

    private AirportEntity createAirport(String iataCode, String name) {
        AirportEntity airport = new AirportEntity();
        airport.setIataCode(iataCode);
        airport.setName(name);
        return airport;
    }

    private AirplaneTypeEntity createAirplaneType(Long id, String producer, String model, AirplaneTypeStatus status) {
        AirplaneTypeEntity entity = new AirplaneTypeEntity();
        entity.setId(id);
        entity.setProducer(producer);
        entity.setModel(model);
        entity.setStatus(status);
        return entity;
    }

    @DisplayName("Should return all airplane types when admin")
    @Test
    void shouldReturnAllAirplaneTypesForAdmin() {
        List<AirportEntity> airportEntities = List.of(createAirport("BOG", "El Dorado"));
        List<AirportSearchOptionDto> airportDtos = List.of(new AirportSearchOptionDto("BOG", "Bogota", "El Dorado"));
        List<AirplaneTypeEntity> typeEntities = List.of(
                createAirplaneType(1L, "Boeing", "737", AirplaneTypeStatus.ACTIVE),
                createAirplaneType(2L, "Airbus", "A320", AirplaneTypeStatus.INACTIVE)
        );
        List<AirplaneTypeOptionDto> typeDtos = List.of(
                new AirplaneTypeOptionDto(1L, "Boeing", "737"),
                new AirplaneTypeOptionDto(2L, "Airbus", "A320")
        );
        List<CountryEntity> countryEntities = List.of(new CountryEntity());
        List<CountryDto> countryDtos = List.of(new CountryDto("Colombia", "CO"));

        given(airportRepository.findAll()).willReturn(airportEntities);
        given(airportMapper.toSearchOptionDto(airportEntities)).willReturn(airportDtos);
        given(airplaneTypeRepository.findAll()).willReturn(typeEntities);
        given(airplaneTypeMapper.toOptionDto(typeEntities)).willReturn(typeDtos);
        given(countryRepository.findAllByOrderByNameAsc()).willReturn(countryEntities);
        given(countryMapper.toDto(countryEntities)).willReturn(countryDtos);

        CatalogDropdownDto result = catalogService.getDropdownOptions(true);

        assertThat(result.airports()).hasSize(1).contains(new AirportSearchOptionDto("BOG", "Bogota", "El Dorado"));
        assertThat(result.airplaneTypes()).hasSize(2);
        assertThat(result.countries()).hasSize(1).contains(new CountryDto("Colombia", "CO"));
        verify(airplaneTypeRepository).findAll();
    }

    @DisplayName("Should return only active airplane types for non-admin")
    @Test
    void shouldReturnOnlyActiveAirplaneTypesForNonAdmin() {
        List<AirportEntity> airportEntities = List.of(createAirport("CTG", "Rafael Nunez"));
        List<AirportSearchOptionDto> airportDtos = List.of(new AirportSearchOptionDto("CTG", "Cartagena", "Rafael Nunez"));
        List<AirplaneTypeEntity> activeEntities = List.of(
                createAirplaneType(1L, "Boeing", "737", AirplaneTypeStatus.ACTIVE)
        );
        List<AirplaneTypeOptionDto> activeDtos = List.of(
                new AirplaneTypeOptionDto(1L, "Boeing", "737")
        );
        List<CountryEntity> countryEntities = List.of(new CountryEntity());
        List<CountryDto> countryDtos = List.of(new CountryDto("Colombia", "CO"));

        given(airportRepository.findAll()).willReturn(airportEntities);
        given(airportMapper.toSearchOptionDto(airportEntities)).willReturn(airportDtos);
        given(airplaneTypeRepository.findByStatus(AirplaneTypeStatus.ACTIVE)).willReturn(activeEntities);
        given(airplaneTypeMapper.toOptionDto(activeEntities)).willReturn(activeDtos);
        given(countryRepository.findAllByOrderByNameAsc()).willReturn(countryEntities);
        given(countryMapper.toDto(countryEntities)).willReturn(countryDtos);

        CatalogDropdownDto result = catalogService.getDropdownOptions(false);

        assertThat(result.airplaneTypes()).hasSize(1);
        assertThat(result.airplaneTypes().getFirst().id()).isEqualTo(1L);
        verify(airplaneTypeRepository).findByStatus(AirplaneTypeStatus.ACTIVE);
    }

    @DisplayName("Should return origin airports delegating to routeQueryService")
    @Test
    void shouldReturnOriginAirports() {
        List<AirportSearchOptionDto> expected = List.of(
                new AirportSearchOptionDto("BOG", "Bogota", "El Dorado")
        );
        given(routeQueryService.getOriginAirports()).willReturn(expected);

        List<AirportSearchOptionDto> result = catalogService.getOriginAirports();

        assertThat(result).isEqualTo(expected);
        verify(routeQueryService).getOriginAirports();
    }

    @DisplayName("Should return destination airports delegating to routeQueryService")
    @Test
    void shouldReturnDestinationAirports() {
        List<AirportSearchOptionDto> expected = List.of(
                new AirportSearchOptionDto("MDE", "Medellin", "Jose Maria Cordoba")
        );
        given(routeQueryService.getDestinationAirports("BOG")).willReturn(expected);

        List<AirportSearchOptionDto> result = catalogService.getDestinationAirports("BOG");

        assertThat(result).isEqualTo(expected);
        verify(routeQueryService).getDestinationAirports("BOG");
    }
}