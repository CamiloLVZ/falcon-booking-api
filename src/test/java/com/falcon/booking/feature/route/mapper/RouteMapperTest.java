package com.falcon.booking.feature.route.mapper;

import com.falcon.booking.common.enums.RouteStatus;
import com.falcon.booking.feature.route.dto.CreateRouteDto;
import com.falcon.booking.feature.route.dto.ResponseRouteDto;
import com.falcon.booking.persistence.entity.AirplaneTypeEntity;
import com.falcon.booking.persistence.entity.AirportEntity;
import com.falcon.booking.persistence.entity.RouteEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class RouteMapperTest {

    @InjectMocks
    private RouteMapperImpl routeMapper;

    @DisplayName("Should map CreateRouteDto to entity")
    @Test
    void shouldMapCreateDtoToEntity() {
        CreateRouteDto dto = new CreateRouteDto(
                "AV1234", "BOG", "MIA", 1L, 180,
                BigDecimal.valueOf(100), BigDecimal.valueOf(200)
        );

        RouteEntity result = routeMapper.toEntity(dto);

        assertThat(result.getFlightNumber()).isEqualTo("AV1234");
        assertThat(result.getDurationMinutes()).isEqualTo(180);
        assertThat(result.getBasePriceEconomy()).isEqualByComparingTo(BigDecimal.valueOf(100));
        assertThat(result.getBasePriceFirstClass()).isEqualByComparingTo(BigDecimal.valueOf(200));
    }

    @DisplayName("Should map entity to ResponseRouteDto")
    @Test
    void shouldMapToResponseDto() {
        AirportEntity origin = new AirportEntity();
        origin.setId(1L);
        origin.setIataCode("BOG");
        origin.setName("El Dorado");
        origin.setCity("Bogota");
        origin.setTimezone("America/Bogota");

        AirportEntity destination = new AirportEntity();
        destination.setId(2L);
        destination.setIataCode("MIA");
        destination.setName("Miami Intl");
        destination.setCity("Miami");
        destination.setTimezone("America/New_York");

        AirplaneTypeEntity airplaneType = new AirplaneTypeEntity();
        airplaneType.setId(1L);
        airplaneType.setProducer("AIRBUS");
        airplaneType.setModel("A320");
        airplaneType.setEconomySeats(150);
        airplaneType.setFirstClassSeats(20);
        airplaneType.setSeatColumns("ABCDEF");

        RouteEntity entity = new RouteEntity();
        entity.setFlightNumber("AV1234");
        entity.setAirportOrigin(origin);
        entity.setAirportDestination(destination);
        entity.setDefaultAirplaneType(airplaneType);
        entity.setDurationMinutes(180);
        entity.setStatus(RouteStatus.ACTIVE);
        entity.setBasePriceEconomy(BigDecimal.valueOf(100));
        entity.setBasePriceFirstClass(BigDecimal.valueOf(200));

        ResponseRouteDto result = routeMapper.toResponseDto(entity);

        assertThat(result.flightNumber()).isEqualTo("AV1234");
        assertThat(result.airportOrigin().iataCode()).isEqualTo("BOG");
        assertThat(result.airportOrigin().name()).isEqualTo("El Dorado");
        assertThat(result.airportDestination().iataCode()).isEqualTo("MIA");
        assertThat(result.defaultAirplaneType().producer()).isEqualTo("AIRBUS");
        assertThat(result.durationMinutes()).isEqualTo(180);
        assertThat(result.status()).isEqualTo(RouteStatus.ACTIVE);
        assertThat(result.basePriceEconomy()).isEqualByComparingTo(BigDecimal.valueOf(100));
        assertThat(result.basePriceFirstClass()).isEqualByComparingTo(BigDecimal.valueOf(200));
    }

    @DisplayName("Should map entity list to ResponseRouteDto list")
    @Test
    void shouldMapListToResponseDto() {
        AirportEntity origin = new AirportEntity();
        origin.setId(1L);
        origin.setIataCode("BOG");
        origin.setName("El Dorado");
        origin.setCity("Bogota");
        origin.setTimezone("America/Bogota");

        AirportEntity destination = new AirportEntity();
        destination.setId(2L);
        destination.setIataCode("MIA");
        destination.setName("Miami Intl");
        destination.setCity("Miami");
        destination.setTimezone("America/New_York");

        AirplaneTypeEntity airplaneType = new AirplaneTypeEntity();
        airplaneType.setId(1L);
        airplaneType.setProducer("AIRBUS");
        airplaneType.setModel("A320");
        airplaneType.setEconomySeats(150);
        airplaneType.setFirstClassSeats(20);
        airplaneType.setSeatColumns("ABCDEF");

        RouteEntity entity = new RouteEntity();
        entity.setFlightNumber("AV1234");
        entity.setAirportOrigin(origin);
        entity.setAirportDestination(destination);
        entity.setDefaultAirplaneType(airplaneType);
        entity.setDurationMinutes(180);
        entity.setStatus(RouteStatus.ACTIVE);
        entity.setBasePriceEconomy(BigDecimal.valueOf(100));
        entity.setBasePriceFirstClass(BigDecimal.valueOf(200));

        List<ResponseRouteDto> result = routeMapper.toResponseDto(List.of(entity));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).flightNumber()).isEqualTo("AV1234");
    }
}
