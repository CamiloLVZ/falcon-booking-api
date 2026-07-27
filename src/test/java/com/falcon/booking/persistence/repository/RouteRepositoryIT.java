package com.falcon.booking.persistence.repository;

import com.falcon.booking.common.enums.AirplaneTypeStatus;
import com.falcon.booking.common.enums.RouteStatus;
import com.falcon.booking.persistence.entity.AirplaneTypeEntity;
import com.falcon.booking.persistence.entity.AirportEntity;
import com.falcon.booking.persistence.entity.CountryEntity;
import com.falcon.booking.persistence.entity.RouteEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;


import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class RouteRepositoryIT extends BaseRepositoryTest {

    private int sequence = 0;

    @Autowired
    private RouteRepository routeRepository;
    @Autowired
    private CountryRepository countryRepository;
    @Autowired
    private AirportRepository airportRepository;
    @Autowired
    private AirplaneTypeRepository airplaneTypeRepository;

    private CountryEntity country;
    private AirportEntity bogota;
    private AirportEntity medellin;
    private AirportEntity cali;
    private AirportEntity cartagena;
    private AirplaneTypeEntity airplaneType;

    private CountryEntity createCountry(String isoCode, String name) {
        CountryEntity c = new CountryEntity();
        c.setIsoCode(isoCode);
        c.setName(name);
        return c;
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
        AirplaneTypeEntity type = new AirplaneTypeEntity();
        type.setProducer("Airbus " + suffix);
        type.setModel("A320-" + suffix);
        type.configureSeats(108, 12, "ABCDEF");
        type.setStatus(AirplaneTypeStatus.ACTIVE);
        return type;
    }

    private RouteEntity createRoute(String flightNumber, RouteStatus status) {
        sequence++;
        CountryEntity c = countryRepository.save(createCountry("C" + sequence, "Country " + sequence));
        AirportEntity origin = airportRepository.save(createAirport("B" + sequence, "Origin " + sequence, "City1", c));
        AirportEntity destination = airportRepository.save(createAirport("M" + sequence, "Dest " + sequence, "City2", c));
        AirplaneTypeEntity type = airplaneTypeRepository.save(createAirplaneType(sequence));

        return createRoute(flightNumber, status, origin, destination, type);
    }

    private RouteEntity createRoute(String flightNumber, RouteStatus status, AirportEntity origin, AirportEntity destination, AirplaneTypeEntity type) {
        RouteEntity route = new RouteEntity();
        route.setFlightNumber(flightNumber);
        route.setAirportOrigin(origin);
        route.setAirportDestination(destination);
        route.setDefaultAirplaneType(type);
        route.setDurationMinutes(60);
        route.setStatus(status);
        return route;
    }

    @BeforeEach
    public void setup() {
        country = countryRepository.findByIsoCode("CO")
                .orElseGet(() -> countryRepository.save(createCountry("CO", "Colombia")));

        bogota = airportRepository.findByIataCode("BOG")
                .orElseGet(() -> airportRepository.save(createAirport("BOG", "El Dorado", "Bogota", country)));

        medellin = airportRepository.findByIataCode("MDE")
                .orElseGet(() -> airportRepository.save(createAirport("MDE", "Jose Maria Cordoba", "Medellin", country)));

        cali = airportRepository.findByIataCode("CLO")
                .orElseGet(() -> airportRepository.save(createAirport("CLO", "Alfonso Bonilla Aragon", "Cali", country)));

        cartagena = airportRepository.findByIataCode("CTG")
                .orElseGet(() -> airportRepository.save(createAirport("CTG", "Rafael Nunez", "Cartagena", country)));

        airplaneType = airplaneTypeRepository.save(createAirplaneType(100));
    }

    @DisplayName("Should return route when flight number exists")
    @Test
    void shouldReturnRoute_findByFlightNumber() {
        RouteEntity route = routeRepository.save(createRoute("AV1001", RouteStatus.ACTIVE));

        Optional<RouteEntity> routeFound = routeRepository.findByFlightNumber("AV1001");

        assertThat(routeFound).isPresent();
        assertThat(routeFound.get().getFlightNumber()).isEqualTo(route.getFlightNumber());
    }

    @DisplayName("Should return empty optional when flight number does not exist")
    @Test
    void shouldReturnEmptyOptional_findByFlightNumber() {
        routeRepository.save(createRoute("AV1002", RouteStatus.ACTIVE));

        Optional<RouteEntity> routeFound = routeRepository.findByFlightNumber("AV9999");

        assertThat(routeFound).isEmpty();
    }

    @DisplayName("Should return true when route exists by flight number")
    @Test
    void shouldReturnTrue_existsByFlightNumber() {
        routeRepository.save(createRoute("AV1003", RouteStatus.DRAFT));

        boolean exists = routeRepository.existsByFlightNumber("AV1003");

        assertThat(exists).isTrue();
    }

    @DisplayName("Should return false when route does not exist by flight number")
    @Test
    void shouldReturnFalse_existsByFlightNumber() {
        routeRepository.save(createRoute("AV1004", RouteStatus.DRAFT));

        boolean exists = routeRepository.existsByFlightNumber("AV9999");

        assertThat(exists).isFalse();
    }

    @DisplayName("Should return route list by status")
    @Test
    void shouldReturnRouteList_findAllByStatus() {
        routeRepository.save(createRoute("AV1005", RouteStatus.ACTIVE));
        routeRepository.save(createRoute("AV1006", RouteStatus.DRAFT));

        List<RouteEntity> routes = routeRepository.findAllByStatus(RouteStatus.ACTIVE);

        assertThat(routes).hasSize(1);
        assertThat(routes.get(0).getFlightNumber()).isEqualTo("AV1005");
    }

    @DisplayName("Should return empty list by status")
    @Test
    void shouldReturnEmptyList_findAllByStatus() {
        routeRepository.save(createRoute("AV1007", RouteStatus.ACTIVE));
        routeRepository.save(createRoute("AV1008", RouteStatus.DRAFT));

        List<RouteEntity> routes = routeRepository.findAllByStatus(RouteStatus.INACTIVE);

        assertThat(routes).isEmpty();
    }

    @DisplayName("Should return distinct origin airports for active routes")
    @Test
    void shouldReturnDistinctOrigins_findDistinctOrigins() {
        // Preparamos datos específicos para esta prueba
        routeRepository.save(createRoute("AV1009", RouteStatus.ACTIVE, bogota, medellin, airplaneType));
        routeRepository.save(createRoute("AV1010", RouteStatus.ACTIVE, bogota, cali, airplaneType));

        List<AirportEntity> origins = routeRepository.findDistinctOrigins();

        assertThat(origins).hasSize(1);
        assertThat(origins.get(0).getIataCode()).isEqualTo("BOG");
    }

    @DisplayName("Should return empty origin airport list when there are no active routes")
    @Test
    void shouldReturnEmptyList_findDistinctOrigins() {
        routeRepository.save(createRoute("AV1011", RouteStatus.INACTIVE, bogota, medellin, airplaneType));

        List<AirportEntity> origins = routeRepository.findDistinctOrigins();

        assertThat(origins).isEmpty();
    }

    @DisplayName("Should return destinations by origin for active routes")
    @Test
    void shouldReturnDestinationsByOrigin_findDestinationsByOrigin() {
        routeRepository.save(createRoute("AV1012", RouteStatus.ACTIVE, bogota, medellin, airplaneType));
        routeRepository.save(createRoute("AV1013", RouteStatus.ACTIVE, bogota, cali, airplaneType));
        routeRepository.save(createRoute("AV1014", RouteStatus.INACTIVE, bogota, cartagena, airplaneType));
        routeRepository.save(createRoute("AV1015", RouteStatus.ACTIVE, medellin, cartagena, airplaneType));

        List<AirportEntity> destinations = routeRepository.findDestinationsByOrigin("BOG");

        assertThat(destinations).hasSize(2);
        assertThat(destinations).extracting(AirportEntity::getIataCode).containsExactlyInAnyOrder("MDE", "CLO");
    }

    @DisplayName("Should return empty destination list when origin has no active routes")
    @Test
    void shouldReturnEmptyList_findDestinationsByOrigin() {
        routeRepository.save(createRoute("AV1016", RouteStatus.INACTIVE, bogota, medellin, airplaneType));

        List<AirportEntity> destinations = routeRepository.findDestinationsByOrigin("BOG");

        assertThat(destinations).isEmpty();
    }
}