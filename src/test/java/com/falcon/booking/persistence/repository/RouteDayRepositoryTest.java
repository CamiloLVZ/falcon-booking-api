package com.falcon.booking.persistence.repository;

import com.falcon.booking.domain.valueobject.AirplaneTypeStatus;
import com.falcon.booking.domain.valueobject.RouteStatus;
import com.falcon.booking.persistence.entity.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.DayOfWeek;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("tests")
public class RouteDayRepositoryTest {

    private int sequence = 0;

    @Autowired
    private RouteDayRepository routeDayRepository;
    @Autowired
    private RouteRepository routeRepository;
    @Autowired
    private CountryRepository countryRepository;
    @Autowired
    private AirportRepository airportRepository;
    @Autowired
    private AirplaneTypeRepository airplaneTypeRepository;

    private RouteEntity createRoute() {
        sequence++;
        CountryEntity country = new CountryEntity();
        country.setIsoCode("C" + sequence);
        country.setName("Colombia " + sequence);
        country = countryRepository.save(country);

        AirportEntity origin = new AirportEntity();
        origin.setIataCode("B" + sequence + "G");
        origin.setName("El Dorado " + sequence);
        origin.setCity("Bogota");
        origin.setCountry(country);
        origin.setTimezone("America/Bogota");
        origin = airportRepository.save(origin);

        AirportEntity destination = new AirportEntity();
        destination.setIataCode("M" + sequence + "E");
        destination.setName("Jose Maria Cordoba " + sequence);
        destination.setCity("Medellin");
        destination.setCountry(country);
        destination.setTimezone("America/Bogota");
        destination = airportRepository.save(destination);

        AirplaneTypeEntity airplaneType = new AirplaneTypeEntity();
        airplaneType.setProducer("Airbus " + sequence);
        airplaneType.setModel("A320-" + sequence);
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

    @DisplayName("Should delete all route days by route")
    @Test
    void shouldDeleteAllByRoute() {
        RouteEntity route = createRoute();
        routeDayRepository.save(new RouteDayEntity(route, DayOfWeek.MONDAY));
        routeDayRepository.save(new RouteDayEntity(route, DayOfWeek.TUESDAY));

        routeDayRepository.deleteAllByRoute(route);
        routeDayRepository.flush();

        assertThat(routeDayRepository.findAll()).isEmpty();
    }
}
