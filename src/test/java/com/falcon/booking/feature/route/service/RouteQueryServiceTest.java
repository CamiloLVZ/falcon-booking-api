package com.falcon.booking.feature.route.service;

import com.falcon.booking.common.enums.AirplaneTypeStatus;
import com.falcon.booking.common.enums.RouteStatus;
import com.falcon.booking.feature.airport.dto.AirportSearchOptionDto;
import com.falcon.booking.feature.airport.mapper.AirportMapper;
import com.falcon.booking.feature.route.dto.ResponseRouteDto;
import com.falcon.booking.feature.route.exception.RouteNotFoundException;
import com.falcon.booking.feature.route.mapper.RouteMapper;
import com.falcon.booking.persistence.entity.*;
import com.falcon.booking.persistence.repository.RouteRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class RouteQueryServiceTest {

    @Mock
    private RouteRepository routeRepository;
    @Mock
    private RouteMapper routeMapper;
    @Mock
    private AirportMapper airportMapper;

    @InjectMocks
    private RouteQueryService routeQueryService;

    private AirplaneTypeEntity createAirplaneType(AirplaneTypeStatus status) {
        AirplaneTypeEntity airplaneType = new AirplaneTypeEntity();
        airplaneType.setId(1L);
        airplaneType.setProducer("Airbus");
        airplaneType.setModel("A320");
        airplaneType.setEconomySeats(100);
        airplaneType.setFirstClassSeats(10);
        airplaneType.setStatus(status);
        return airplaneType;
    }

    private AirportEntity createAirport(Long id, String iataCode) {
        AirportEntity airport = new AirportEntity();
        airport.setId(id);
        airport.setIataCode(iataCode);
        airport.setTimezone("America/Bogota");
        return airport;
    }

    private RouteEntity createRouteEntity(String flightNumber) {
        RouteEntity route = new RouteEntity();
        route.setFlightNumber(flightNumber);
        route.setAirportOrigin(createAirport(1L, "BOG"));
        route.setAirportDestination(createAirport(2L, "MDE"));
        route.setDefaultAirplaneType(createAirplaneType(AirplaneTypeStatus.ACTIVE));
        route.setDurationMinutes(60);
        route.setStatus(RouteStatus.DRAFT);
        route.setRouteDays(new HashSet<>(List.of(new RouteDayEntity(route, DayOfWeek.MONDAY))));
        route.setRouteSchedules(new HashSet<>(List.of(new RouteScheduleEntity(route, LocalTime.of(8, 0)))));
        return route;
    }

    @DisplayName("Should return RouteEntity when flight number exists")
    @Test
    void shouldReturnEntity_getRouteEntity() {
        RouteEntity route = createRouteEntity("AV1234");
        given(routeRepository.findByFlightNumber("AV1234")).willReturn(Optional.of(route));

        RouteEntity result = routeQueryService.getRouteEntity(" av1234 ");

        assertThat(result).isEqualTo(route);
        verify(routeRepository).findByFlightNumber("AV1234");
    }

    @DisplayName("Should throw exception when route does not exist")
    @Test
    void shouldThrowException_getRouteEntity() {
        given(routeRepository.findByFlightNumber("AV9999")).willReturn(Optional.empty());

        RouteNotFoundException exception =
                assertThrows(RouteNotFoundException.class, () -> routeQueryService.getRouteEntity(" av9999 "));

        assertThat(exception.getMessage()).contains("AV9999");
    }

    @DisplayName("Should return route dto when flight number exists")
    @Test
    void shouldReturnDto_getRouteByFlightNumber() {
        RouteEntity route = createRouteEntity("AV1234");
        ResponseRouteDto dto = new ResponseRouteDto("AV1234", null, null, null, 60, RouteStatus.DRAFT);
        given(routeRepository.findByFlightNumber("AV1234")).willReturn(Optional.of(route));
        given(routeMapper.toResponseDto(route)).willReturn(dto);

        ResponseRouteDto result = routeQueryService.getRouteByFlightNumber("av1234");

        assertThat(result).isEqualTo(dto);
        verify(routeMapper).toResponseDto(route);
    }

    @DisplayName("Should return origin airport options")
    @Test
    void shouldReturnOriginAirportOptions_getOriginAirports() {
        List<AirportEntity> airports = List.of(createAirport(1L, "BOG"), createAirport(2L, "MDE"));
        List<AirportSearchOptionDto> airportDtos = List.of(
                new AirportSearchOptionDto("BOG", "Bogota", "El Dorado"),
                new AirportSearchOptionDto("MDE", "Medellin", "Jose Maria Cordoba")
        );
        given(routeRepository.findDistinctOrigins()).willReturn(airports);
        given(airportMapper.toSearchOptionDto(airports)).willReturn(airportDtos);

        List<AirportSearchOptionDto> result = routeQueryService.getOriginAirports();

        assertThat(result).isEqualTo(airportDtos);
        verify(routeRepository).findDistinctOrigins();
        verify(airportMapper).toSearchOptionDto(airports);
    }

    @DisplayName("Should return destination airport options by normalized origin")
    @Test
    void shouldReturnDestinationAirportOptions_getDestinationAirports() {
        List<AirportEntity> airports = List.of(createAirport(2L, "MDE"), createAirport(3L, "CLO"));
        List<AirportSearchOptionDto> airportDtos = List.of(
                new AirportSearchOptionDto("MDE", "Medellin", "Jose Maria Cordoba"),
                new AirportSearchOptionDto("CLO", "Cali", "Alfonso Bonilla Aragon")
        );
        given(routeRepository.findDestinationsByOrigin("BOG")).willReturn(airports);
        given(airportMapper.toSearchOptionDto(airports)).willReturn(airportDtos);

        List<AirportSearchOptionDto> result = routeQueryService.getDestinationAirports(" bog ");

        assertThat(result).isEqualTo(airportDtos);
        verify(routeRepository).findDestinationsByOrigin("BOG");
        verify(airportMapper).toSearchOptionDto(airports);
    }
}
