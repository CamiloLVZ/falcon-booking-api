package com.falcon.booking.persistence.repository;

import com.falcon.booking.domain.valueobject.AirplaneTypeStatus;
import com.falcon.booking.domain.valueobject.RouteStatus;
import com.falcon.booking.persistence.entity.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("tests")
public class RouteRepositoryTest {

    private int sequence = 0;

    @Autowired
    private RouteRepository routeRepository;
    @Autowired
    private CountryRepository countryRepository;
    @Autowired
    private AirportRepository airportRepository;
    @Autowired
    private AirplaneTypeRepository airplaneTypeRepository;

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

    private AirplaneTypeEntity createAirplaneType(int suffix) {
        AirplaneTypeEntity airplaneType = new AirplaneTypeEntity();
        airplaneType.setProducer("Airbus " + suffix);
        airplaneType.setModel("A320-" + suffix);
        airplaneType.setEconomySeats(100);
        airplaneType.setFirstClassSeats(10);
        airplaneType.setStatus(AirplaneTypeStatus.ACTIVE);
        return airplaneType;
    }

    private RouteEntity createRoute(String flightNumber, RouteStatus status) {
        sequence++;
        CountryEntity country = countryRepository.save(createCountry("C" + sequence, "Colombia " + sequence));
        AirportEntity origin = airportRepository.save(createAirport("B" + sequence + "G", "El Dorado " + sequence, "Bogota", country));
        AirportEntity destination = airportRepository.save(createAirport("M" + sequence + "E", "Jose Maria Cordoba " + sequence, "Medellin", country));
        AirplaneTypeEntity airplaneType = airplaneTypeRepository.save(createAirplaneType(sequence));

        RouteEntity route = new RouteEntity();
        route.setFlightNumber(flightNumber);
        route.setAirportOrigin(origin);
        route.setAirportDestination(destination);
        route.setDefaultAirplaneType(airplaneType);
        route.setLengthMinutes(60);
        route.setStatus(status);
        return route;
    }

    @DisplayName("Should return route when flight number exists")
    @Test
    void shouldReturnRoute_findByFlightNumber() {
        RouteEntity route = routeRepository.save(createRoute("AV1234", RouteStatus.ACTIVE));

        Optional<RouteEntity> routeFound = routeRepository.findByFlightNumber("AV1234");

        assertThat(routeFound).isPresent();
        assertThat(routeFound.get().getFlightNumber()).isEqualTo(route.getFlightNumber());
    }

    @DisplayName("Should return empty optional when flight number does not exist")
    @Test
    void shouldReturnEmptyOptional_findByFlightNumber() {
        RouteEntity route = routeRepository.save(createRoute("AV1234", RouteStatus.ACTIVE));

        Optional<RouteEntity> routeFound = routeRepository.findByFlightNumber("AV1514");

        assertThat(routeFound).isNotNull();
        assertThat(routeFound).isEmpty();
    }

    @DisplayName("Should return true when route exists by flight number")
    @Test
    void shouldReturnTrue_existsByFlightNumber() {
        routeRepository.save(createRoute("AV1234", RouteStatus.DRAFT));

        boolean exists = routeRepository.existsByFlightNumber("AV1234");

        assertThat(exists).isTrue();
    }

    @DisplayName("Should return false when route does not exist by flight number")
    @Test
    void shouldReturnFalse_existsByFlightNumber() {
        routeRepository.save(createRoute("AV1234", RouteStatus.DRAFT));

        boolean exists = routeRepository.existsByFlightNumber("AV1215");

        assertThat(exists).isFalse();
    }

    @DisplayName("Should return route list by status")
    @Test
    void shouldReturnRouteList_findAllByStatus() {
        routeRepository.save(createRoute("AV681", RouteStatus.ACTIVE));
        routeRepository.save(createRoute("AV8974", RouteStatus.DRAFT));

        List<RouteEntity> routes = routeRepository.findAllByStatus(RouteStatus.ACTIVE);

        assertThat(routes).hasSize(1);
        assertThat(routes.get(0).getFlightNumber()).isEqualTo("AV681");
    }

    @DisplayName("Should return empty list by status")
    @Test
    void shouldReturnEmptyList_findAllByStatus() {
        routeRepository.save(createRoute("AV1684", RouteStatus.ACTIVE));
        routeRepository.save(createRoute("AV9849", RouteStatus.DRAFT));

        List<RouteEntity> routes = routeRepository.findAllByStatus(RouteStatus.INACTIVE);

        assertThat(routes).isEmpty();
    }

}
