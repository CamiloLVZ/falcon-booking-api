package com.falcon.booking.feature.route.service;

import com.falcon.booking.feature.airplaneType.service.AirplaneTypeService;
import com.falcon.booking.feature.airport.service.AirportService;
import com.falcon.booking.feature.route.exception.RouteAirplaneTypeIsNotActiveException;
import com.falcon.booking.feature.route.exception.RouteAlreadyExistsException;
import com.falcon.booking.feature.route.exception.RouteNotFoundException;
import com.falcon.booking.feature.route.exception.RouteSameOriginAndDestinationException;
import com.falcon.booking.feature.airport.mapper.AirportMapper;
import com.falcon.booking.feature.flightGeneration.service.AsyncFlightGenerationService;
import com.falcon.booking.feature.route.mapper.RouteMapper;
import com.falcon.booking.common.enums.AirplaneTypeStatus;
import com.falcon.booking.common.enums.RouteStatus;
import com.falcon.booking.persistence.entity.*;
import com.falcon.booking.persistence.repository.RouteDayRepository;
import com.falcon.booking.persistence.repository.RouteRepository;
import com.falcon.booking.persistence.repository.RouteScheduleRepository;
import com.falcon.booking.feature.airport.dto.AirportSearchOptionDto;
import com.falcon.booking.feature.route.dto.AddRouteScheduleRequestDto;
import com.falcon.booking.feature.route.dto.CreateRouteDto;
import com.falcon.booking.feature.route.dto.ResponseRouteDto;
import com.falcon.booking.feature.route.dto.RouteWithSchedulesDto;
import com.falcon.booking.feature.route.dto.UpdateRouteDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class RouteServiceTest {

    @Mock
    private RouteRepository routeRepository;
    @Mock
    private RouteDayRepository routeDayRepository;
    @Mock
    private RouteScheduleRepository routeScheduleRepository;
    @Mock
    private RouteMapper routeMapper;
    @Mock
    private AirportMapper airportMapper;
    @Mock
    private AsyncFlightGenerationService asyncFlightGenerationService;
    @Mock
    private AirplaneTypeService airplaneTypeService;
    @Mock
    private AirportService airportService;

    @InjectMocks
    private RouteService routeService;

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

        RouteEntity result = routeService.getRouteEntity(" av1234 ");

        assertThat(result).isEqualTo(route);
        verify(routeRepository).findByFlightNumber("AV1234");
    }

    @DisplayName("Should throw exception when route does not exist")
    @Test
    void shouldThrowException_getRouteEntity() {
        given(routeRepository.findByFlightNumber("AV9999")).willReturn(Optional.empty());

        RouteNotFoundException exception =
                assertThrows(RouteNotFoundException.class, () -> routeService.getRouteEntity(" av9999 "));

        assertThat(exception.getMessage()).contains("AV9999");
    }

    @DisplayName("Should return route dto when flight number exists")
    @Test
    void shouldReturnDto_getRouteByFlightNumber() {
        RouteEntity route = createRouteEntity("AV1234");
        ResponseRouteDto dto = new ResponseRouteDto("AV1234", null, null, null, 60, RouteStatus.DRAFT);
        given(routeRepository.findByFlightNumber("AV1234")).willReturn(Optional.of(route));
        given(routeMapper.toResponseDto(route)).willReturn(dto);

        ResponseRouteDto result = routeService.getRouteByFlightNumber("av1234");

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

        List<AirportSearchOptionDto> result = routeService.getOriginAirports();

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

        List<AirportSearchOptionDto> result = routeService.getDestinationAirports(" bog ");

        assertThat(result).isEqualTo(airportDtos);
        verify(routeRepository).findDestinationsByOrigin("BOG");
        verify(airportMapper).toSearchOptionDto(airports);
    }

    @DisplayName("Should add route when data is valid")
    @Test
    void shouldAddRoute_addRoute() {
        CreateRouteDto createDto = new CreateRouteDto("AV1234", "BOG", "MDE", 1L, 60);
        RouteEntity routeToSave = createRouteEntity("AV1234");
        routeToSave.setStatus(null);
        RouteEntity savedRoute = createRouteEntity("AV1234");
        ResponseRouteDto responseDto = new ResponseRouteDto("AV1234", null, null, null, 60, RouteStatus.DRAFT);
        AirplaneTypeEntity airplaneType = createAirplaneType(AirplaneTypeStatus.ACTIVE);
        AirportEntity origin = createAirport(1L, "BOG");
        AirportEntity destination = createAirport(2L, "MDE");

        given(routeRepository.existsByFlightNumber("AV1234")).willReturn(false);
        given(airplaneTypeService.getAirplaneTypeEntity(1L)).willReturn(airplaneType);
        given(airportService.getAirportEntityByIataCode("BOG")).willReturn(origin);
        given(airportService.getAirportEntityByIataCode("MDE")).willReturn(destination);
        given(routeMapper.toEntity(createDto)).willReturn(routeToSave);
        given(routeRepository.save(routeToSave)).willReturn(savedRoute);
        given(routeMapper.toResponseDto(savedRoute)).willReturn(responseDto);

        ResponseRouteDto result = routeService.addRoute(createDto);

        assertThat(result).isEqualTo(responseDto);
        assertThat(routeToSave.isDraft()).isTrue();
    }

    @DisplayName("Should throw exception when route already exists")
    @Test
    void shouldThrowException_addRoute() {
        CreateRouteDto createDto = new CreateRouteDto("AV1234", "BOG", "MDE", 1L, 60);
        given(routeRepository.existsByFlightNumber("AV1234")).willReturn(true);

        assertThrows(RouteAlreadyExistsException.class, () -> routeService.addRoute(createDto));
        verify(routeRepository, never()).save(any());
    }

    @DisplayName("Should throw exception when route has same origin and destination")
    @Test
    void shouldThrowExceptionSameOriginAndDestination_addRoute() {
        CreateRouteDto createDto = new CreateRouteDto("AV1234", "BOG", "BOG", 1L, 60);
        given(routeRepository.existsByFlightNumber(anyString())).willReturn(false);

        assertThrows(RouteSameOriginAndDestinationException.class,

                () -> routeService.addRoute(createDto));
    }

    @DisplayName("Should throw exception when airplane type is not active")
    @Test
    void shouldThrowExceptionAirplaneNotActive_addRoute() {
        CreateRouteDto createDto = new CreateRouteDto("AV1234", "BOG", "MDE", 1L, 60);
        AirplaneTypeEntity airplaneType = createAirplaneType(AirplaneTypeStatus.INACTIVE);

        given(routeRepository.existsByFlightNumber("AV1234")).willReturn(false);
        given(airplaneTypeService.getAirplaneTypeEntity(1L)).willReturn(airplaneType);

        assertThrows(RouteAirplaneTypeIsNotActiveException.class, () -> routeService.addRoute(createDto));
    }

    @DisplayName("Should update route when data is valid")
    @Test
    void shouldUpdateRoute_updateRoute() {
        RouteEntity route = createRouteEntity("AV1234");
        UpdateRouteDto updateDto = new UpdateRouteDto(null, null, null, 90);
        ResponseRouteDto responseDto = new ResponseRouteDto("AV1234", null, null, null, 90, RouteStatus.DRAFT);
        given(routeRepository.findByFlightNumber("AV1234")).willReturn(Optional.of(route));
        given(routeMapper.toResponseDto(route)).willReturn(responseDto);

        ResponseRouteDto result = routeService.updateRoute("AV1234", updateDto);

        assertThat(result).isEqualTo(responseDto);
        assertThat(route.getDurationMinutes()).isEqualTo(90);
    }



    @DisplayName("Should deactivate route")
    @Test
    void shouldDeactivateRoute_deactivateRoute() {
        RouteEntity route = createRouteEntity("AV1234");
        route.setStatus(RouteStatus.ACTIVE);
        ResponseRouteDto responseDto = new ResponseRouteDto("AV1234", null, null, null, 60, RouteStatus.INACTIVE);

        given(routeRepository.findByFlightNumber("AV1234")).willReturn(Optional.of(route));
        given(routeMapper.toResponseDto(route)).willReturn(responseDto);

        ResponseRouteDto result = routeService.deactivateRoute("AV1234");

        assertThat(result).isEqualTo(responseDto);
        assertThat(route.isInactive()).isTrue();
    }

    @DisplayName("Should set route operating schedules")
    @Test
    void shouldSetRouteOperatingSchedules_setRouteOperatingSchedules() {
        RouteEntity route = createRouteEntity("AV1234");
        AddRouteScheduleRequestDto requestDto = new AddRouteScheduleRequestDto(
                Set.of(LocalTime.of(10, 0), LocalTime.of(15, 0)),
                Set.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY)
        );

        given(routeRepository.findByFlightNumber("AV1234")).willReturn(Optional.of(route));

        RouteWithSchedulesDto result = routeService.setRouteOperatingSchedules("AV1234", requestDto);

        verify(routeDayRepository).deleteAllByRoute(route);
        verify(routeScheduleRepository).deleteAllByRoute(route);
        assertThat(result.flightNumber()).isEqualTo("AV1234");
        assertThat(result.daysOfWeek()).hasSize(2);
        assertThat(result.schedules()).hasSize(2);
    }

    @DisplayName("Should return route with schedules")
    @Test
    void shouldReturnRouteWithSchedules_getRouteWithSchedules() {
        RouteEntity route = createRouteEntity("AV1234");
        given(routeRepository.findByFlightNumber("AV1234")).willReturn(Optional.of(route));

        RouteWithSchedulesDto result = routeService.getRouteWithSchedules("AV1234");

        assertThat(result.flightNumber()).isEqualTo("AV1234");
        assertThat(result.daysOfWeek()).contains(DayOfWeek.MONDAY);
        assertThat(result.schedules()).contains(LocalTime.of(8, 0));
    }
}
