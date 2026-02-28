package com.falcon.booking.persistence.repository;

import com.falcon.booking.domain.valueobject.AirplaneTypeStatus;
import com.falcon.booking.domain.valueobject.RouteStatus;
import com.falcon.booking.persistence.entity.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("tests")
public class RouteScheduleRepositoryTest {

    @Autowired
    private RouteScheduleRepository routeScheduleRepository;
    @Autowired
    private RouteRepository routeRepository;
    @Autowired
    private CountryRepository countryRepository;
    @Autowired
    private AirportRepository airportRepository;
    @Autowired
    private AirplaneTypeRepository airplaneTypeRepository;

    private RouteEntity createRoute() {
        CountryEntity country = new CountryEntity();
        country.setIsoCode("CO");
        country.setName("Colombia");
        country = countryRepository.save(country);

        AirportEntity origin = new AirportEntity();
        origin.setIataCode("BOG");
        origin.setName("El Dorado");
        origin.setCity("Bogota");
        origin.setCountry(country);
        origin.setTimezone("America/Bogota");
        origin = airportRepository.save(origin);

        AirportEntity destination = new AirportEntity();
        destination.setIataCode("MDE");
        destination.setName("Jose Maria Cordoba");
        destination.setCity("Medellin");
        destination.setCountry(country);
        destination.setTimezone("America/Bogota");
        destination = airportRepository.save(destination);

        AirplaneTypeEntity airplaneType = new AirplaneTypeEntity();
        airplaneType.setProducer("Airbus");
        airplaneType.setModel("A320");
        airplaneType.setEconomySeats(100);
        airplaneType.setFirstClassSeats(10);
        airplaneType.setStatus(AirplaneTypeStatus.ACTIVE);
        airplaneType = airplaneTypeRepository.save(airplaneType);

        RouteEntity route = new RouteEntity();
        route.setFlightNumber("AV1234");
        route.setAirportOrigin(origin);
        route.setAirportDestination(destination);
        route.setDefaultAirplaneType(airplaneType);
        route.setLengthMinutes(60);
        route.setStatus(RouteStatus.DRAFT);
        return routeRepository.save(route);
    }

    @DisplayName("Should delete all route schedules by route")
    @Test
    void shouldDeleteAllByRoute() {
        RouteEntity route = createRoute();
        routeScheduleRepository.save(new RouteScheduleEntity(route, LocalTime.of(8, 0)));
        routeScheduleRepository.save(new RouteScheduleEntity(route, LocalTime.of(9, 0)));

        routeScheduleRepository.deleteAllByRoute(route);
        routeScheduleRepository.flush();

        assertThat(routeScheduleRepository.findAll()).isEmpty();
    }
}
