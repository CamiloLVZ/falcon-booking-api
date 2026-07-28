package com.falcon.booking.persistence.specification;

import com.falcon.booking.common.enums.AirplaneTypeStatus;
import com.falcon.booking.common.enums.FlightGenerationStatus;
import com.falcon.booking.common.enums.FlightGenerationType;
import com.falcon.booking.common.enums.RouteStatus;
import com.falcon.booking.persistence.entity.*;
import com.falcon.booking.persistence.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FlightGenerationSpecificationsIT extends BaseRepositoryTest {

    @Autowired
    private FlightGenerationRepository flightGenerationRepository;
    @Autowired
    private RouteRepository routeRepository;
    @Autowired
    private CountryRepository countryRepository;
    @Autowired
    private AirportRepository airportRepository;
    @Autowired
    private AirplaneTypeRepository airplaneTypeRepository;

    private RouteEntity route;
    private RouteEntity otherRoute;

    @BeforeEach
    void setUp() {
        CountryEntity country = countryRepository.findByIsoCode("CO")
                .orElseGet(() -> countryRepository.save(createCountry("CO", "Colombia")));

        AirportEntity bogota = airportRepository.findByIataCode("BOG")
                .orElseGet(() -> airportRepository.save(createAirport("BOG", country)));
        AirportEntity medellin = airportRepository.findByIataCode("MDE")
                .orElseGet(() -> airportRepository.save(createAirport("MDE", country)));

        AirplaneTypeEntity airplaneType = airplaneTypeRepository.findAll().stream().findFirst()
                .orElseGet(() -> {
                    AirplaneTypeEntity at = new AirplaneTypeEntity();
                    at.setProducer("Airbus");
                    at.setModel("A320");
                    at.configureSeats(108, 12, "ABCDEF");
                    at.setStatus(AirplaneTypeStatus.ACTIVE);
                    return airplaneTypeRepository.save(at);
                });

        route = routeRepository.findByFlightNumber("AV100")
                .orElseGet(() -> routeRepository.save(createRoute("AV100", bogota, medellin, airplaneType)));
        otherRoute = routeRepository.findByFlightNumber("AV200")
                .orElseGet(() -> routeRepository.save(createRoute("AV200", bogota, medellin, airplaneType)));

        FlightGenerationEntity g1 = FlightGenerationEntity.startRouteGeneration(route.getId());
        g1.setStatus(FlightGenerationStatus.COMPLETED);
        g1.setFinishedAt(Instant.now());
        g1.setTotalGenerated(5);
        flightGenerationRepository.save(g1);

        FlightGenerationEntity g2 = FlightGenerationEntity.startRouteGeneration(otherRoute.getId());
        g2.setStatus(FlightGenerationStatus.FAILED);
        g2.setFinishedAt(Instant.now());
        flightGenerationRepository.save(g2);

        FlightGenerationEntity g3 = FlightGenerationEntity.startDailyGeneration(LocalDate.of(2026, 7, 27));
        g3.setStatus(FlightGenerationStatus.RUNNING);
        flightGenerationRepository.save(g3);

        FlightGenerationEntity g4 = FlightGenerationEntity.startGlobalGeneration();
        g4.setStatus(FlightGenerationStatus.COMPLETED);
        g4.setFinishedAt(Instant.now());
        g4.setTotalGenerated(100);
        flightGenerationRepository.save(g4);
    }

    private CountryEntity createCountry(String isoCode, String name) {
        CountryEntity c = new CountryEntity();
        c.setIsoCode(isoCode);
        c.setName(name);
        return c;
    }

    private AirportEntity createAirport(String iataCode, CountryEntity country) {
        AirportEntity airport = new AirportEntity();
        airport.setIataCode(iataCode);
        airport.setName("Airport " + iataCode);
        airport.setCity("City " + iataCode);
        airport.setCountry(country);
        airport.setTimezone("America/Bogota");
        return airport;
    }

    private RouteEntity createRoute(String flightNumber, AirportEntity origin, AirportEntity destination, AirplaneTypeEntity airplaneType) {
        RouteEntity entity = new RouteEntity();
        entity.setFlightNumber(flightNumber);
        entity.setAirportOrigin(origin);
        entity.setAirportDestination(destination);
        entity.setDefaultAirplaneType(airplaneType);
        entity.setDurationMinutes(60);
        entity.setStatus(RouteStatus.ACTIVE);
        return entity;
    }

    @DisplayName("Should filter by type")
    @Test
    void shouldFilterByType() {
        Specification<FlightGenerationEntity> spec = FlightGenerationSpecifications.hasType(FlightGenerationType.ROUTE);

        List<FlightGenerationEntity> result = flightGenerationRepository.findAll(spec);

        assertThat(result).hasSize(2);
        result.forEach(g -> assertThat(g.getType()).isEqualTo(FlightGenerationType.ROUTE));
    }

    @DisplayName("Should return all when type is null")
    @Test
    void shouldReturnAllWhenTypeIsNull() {
        Specification<FlightGenerationEntity> spec = FlightGenerationSpecifications.hasType(null);

        List<FlightGenerationEntity> result = flightGenerationRepository.findAll(spec);

        assertThat(result).hasSize(4);
    }

    @DisplayName("Should filter by status")
    @Test
    void shouldFilterByStatus() {
        Specification<FlightGenerationEntity> spec = FlightGenerationSpecifications.hasStatus(FlightGenerationStatus.COMPLETED);

        List<FlightGenerationEntity> result = flightGenerationRepository.findAll(spec);

        assertThat(result).hasSize(2);
        result.forEach(g -> assertThat(g.getStatus()).isEqualTo(FlightGenerationStatus.COMPLETED));
    }

    @DisplayName("Should return all when status is null")
    @Test
    void shouldReturnAllWhenStatusIsNull() {
        Specification<FlightGenerationEntity> spec = FlightGenerationSpecifications.hasStatus(null);

        List<FlightGenerationEntity> result = flightGenerationRepository.findAll(spec);

        assertThat(result).hasSize(4);
    }

    @DisplayName("Should filter by route ID")
    @Test
    void shouldFilterByRouteId() {
        Specification<FlightGenerationEntity> spec = FlightGenerationSpecifications.hasRouteId(route.getId());

        List<FlightGenerationEntity> result = flightGenerationRepository.findAll(spec);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getIdRoute()).isEqualTo(route.getId());
    }

    @DisplayName("Should return all when route ID is null")
    @Test
    void shouldReturnAllWhenRouteIdIsNull() {
        Specification<FlightGenerationEntity> spec = FlightGenerationSpecifications.hasRouteId(null);

        List<FlightGenerationEntity> result = flightGenerationRepository.findAll(spec);

        assertThat(result).hasSize(4);
    }

    @DisplayName("Should combine type and status filters")
    @Test
    void shouldCombineTypeAndStatus() {
        Specification<FlightGenerationEntity> spec = FlightGenerationSpecifications.hasType(FlightGenerationType.ROUTE)
                .and(FlightGenerationSpecifications.hasStatus(FlightGenerationStatus.COMPLETED));

        List<FlightGenerationEntity> result = flightGenerationRepository.findAll(spec);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getIdRoute()).isEqualTo(route.getId());
    }

    @DisplayName("Should combine all three filters")
    @Test
    void shouldCombineAllFilters() {
        Specification<FlightGenerationEntity> spec = FlightGenerationSpecifications.hasType(FlightGenerationType.ROUTE)
                .and(FlightGenerationSpecifications.hasStatus(FlightGenerationStatus.COMPLETED))
                .and(FlightGenerationSpecifications.hasRouteId(route.getId()));

        List<FlightGenerationEntity> result = flightGenerationRepository.findAll(spec);

        assertThat(result).hasSize(1);
    }

    @DisplayName("Should return empty when no route ID matches")
    @Test
    void shouldReturnEmptyWhenNoRouteMatch() {
        Specification<FlightGenerationEntity> spec = FlightGenerationSpecifications.hasRouteId(999L);

        List<FlightGenerationEntity> result = flightGenerationRepository.findAll(spec);

        assertThat(result).isEmpty();
    }

    @DisplayName("Should filter by global type")
    @Test
    void shouldFilterByGlobalType() {
        Specification<FlightGenerationEntity> spec = FlightGenerationSpecifications.hasType(FlightGenerationType.GLOBAL);

        List<FlightGenerationEntity> result = flightGenerationRepository.findAll(spec);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getType()).isEqualTo(FlightGenerationType.GLOBAL);
    }
}