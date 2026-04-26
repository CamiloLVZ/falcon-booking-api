package com.falcon.booking.domain.mapper;

import com.falcon.booking.domain.valueobject.FlightStatus;
import com.falcon.booking.persistence.entity.AirplaneTypeEntity;
import com.falcon.booking.persistence.entity.AirportEntity;
import com.falcon.booking.persistence.entity.FlightEntity;
import com.falcon.booking.persistence.entity.RouteEntity;
import com.falcon.booking.web.dto.airplaneType.AirplaneTypeInFlightDto;
import com.falcon.booking.web.dto.flight.ResponseFlightDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class FlightMapperTest {

    @Mock
    private AirplaneTypeMapper airplaneTypeMapper;

    @InjectMocks
    private FlightMapper flightMapper;

    @DisplayName("Should map flight entity to dto with local departure datetime")
    @Test
    void shouldMapFlightEntityToDto() {
        AirplaneTypeEntity airplaneType = new AirplaneTypeEntity();
        AirplaneTypeInFlightDto airplaneTypeDto = new AirplaneTypeInFlightDto("Boeing", "737", 150, 12);
        AirportEntity origin = new AirportEntity();
        origin.setIataCode("BOG");
        origin.setTimezone("America/Bogota");

        AirportEntity destination = new AirportEntity();
        destination.setIataCode("MIA");

        RouteEntity route = new RouteEntity();
        route.setFlightNumber("FL123");
        route.setAirportOrigin(origin);
        route.setAirportDestination(destination);
        route.setLengthMinutes(40);

        OffsetDateTime departureDateTime = OffsetDateTime.of(2025, 1, 10, 15, 0, 0, 0, ZoneOffset.UTC);

        FlightEntity flightEntity = new FlightEntity();
        flightEntity.setId(1L);
        flightEntity.setRoute(route);
        flightEntity.setAirplaneType(airplaneType);
        flightEntity.setDepartureDateTime(departureDateTime);
        flightEntity.setStatus(FlightStatus.SCHEDULED);

        given(airplaneTypeMapper.toInFlightDto(airplaneType)).willReturn(airplaneTypeDto);

        ResponseFlightDto result = flightMapper.toDto(flightEntity);

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.flightNumber()).isEqualTo("FL123");
        assertThat(result.origin()).isEqualTo("BOG");
        assertThat(result.destination()).isEqualTo("MIA");
        assertThat(result.departureDateTime()).isEqualTo(departureDateTime);
        assertThat(result.localDepartureDateTime()).isEqualTo(LocalDateTime.of(2025, 1, 10, 10, 0));
        assertThat(result.airplaneType()).isEqualTo(airplaneTypeDto);
        assertThat(result.status()).isEqualTo(FlightStatus.SCHEDULED);
        assertThat(result.durationMinutes()).isEqualTo(40);
    }

    @DisplayName("Should map a list of flights to dto list")
    @Test
    void shouldMapFlightEntityListToDtoList() {
        AirplaneTypeEntity airplaneType = new AirplaneTypeEntity();
        AirplaneTypeInFlightDto airplaneTypeDto = new AirplaneTypeInFlightDto("Airbus", "A320", 160, 8);

        AirportEntity origin = new AirportEntity();
        origin.setIataCode("MAD");
        origin.setTimezone("Europe/Madrid");

        AirportEntity destination = new AirportEntity();
        destination.setIataCode("BCN");

        RouteEntity route = new RouteEntity();
        route.setFlightNumber("IB100");
        route.setAirportOrigin(origin);
        route.setAirportDestination(destination);
        route.setLengthMinutes(40);

        FlightEntity first = new FlightEntity();
        first.setRoute(route);
        first.setAirplaneType(airplaneType);
        first.setDepartureDateTime(OffsetDateTime.of(2025, 2, 2, 12, 0, 0, 0, ZoneOffset.UTC));
        first.setStatus(FlightStatus.SCHEDULED);

        FlightEntity second = new FlightEntity();
        second.setRoute(route);
        second.setAirplaneType(airplaneType);
        second.setDepartureDateTime(OffsetDateTime.of(2025, 2, 3, 12, 0, 0, 0, ZoneOffset.UTC));
        second.setStatus(FlightStatus.CANCELED);

        given(airplaneTypeMapper.toInFlightDto(airplaneType)).willReturn(airplaneTypeDto);

        List<ResponseFlightDto> result = flightMapper.toDto(List.of(first, second));

        assertThat(result).hasSize(2);
        assertThat(result.get(0).status()).isEqualTo(FlightStatus.SCHEDULED);
        assertThat(result.get(1).status()).isEqualTo(FlightStatus.CANCELED);
    }
}
