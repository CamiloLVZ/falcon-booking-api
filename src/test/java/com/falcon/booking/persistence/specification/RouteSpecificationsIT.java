package com.falcon.booking.persistence.specification;

import com.falcon.booking.common.enums.AirplaneTypeStatus;
import com.falcon.booking.common.enums.RouteStatus;
import com.falcon.booking.persistence.entity.AirplaneTypeEntity;
import com.falcon.booking.persistence.entity.AirportEntity;
import com.falcon.booking.persistence.entity.CountryEntity;
import com.falcon.booking.persistence.entity.RouteEntity;
import com.falcon.booking.persistence.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RouteSpecificationsIT extends BaseRepositoryTest {

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
    private AirportEntity miami;
    private AirplaneTypeEntity airplaneType;
    private RouteEntity routeBogMia;
    private RouteEntity routeMdeMia;
    private RouteEntity routeBogMde;

    @BeforeEach
    void setUp() {
        country = countryRepository.findByIsoCode("CO")
                .orElseGet(() -> countryRepository.save(createCountry("CO", "Colombia")));

        bogota = airportRepository.findByIataCode("BOG")
                .orElseGet(() -> airportRepository.save(createAirport("BOG", country)));

        medellin = airportRepository.findByIataCode("MDE")
                .orElseGet(() -> airportRepository.save(createAirport("MDE", country)));

        miami = airportRepository.findByIataCode("MIA")
                .orElse(null);
        if (miami == null) {
            CountryEntity usa = countryRepository.findByIsoCode("US")
                    .orElseGet(() -> countryRepository.save(createCountry("US", "United States")));
            miami = airportRepository.save(createAirport("MIA", usa));
        }

        airplaneType = new AirplaneTypeEntity();
        airplaneType.setProducer("Airbus");
        airplaneType.setModel("A320");
        airplaneType.configureSeats(108, 12, "ABCDEF");
        airplaneType.setStatus(AirplaneTypeStatus.ACTIVE);
        airplaneType = airplaneTypeRepository.save(airplaneType);

        routeBogMia = routeRepository.save(createRoute("AV100", RouteStatus.ACTIVE, bogota, miami));
        routeMdeMia = routeRepository.save(createRoute("AV200", RouteStatus.ACTIVE, medellin, miami));
        routeBogMde = routeRepository.save(createRoute("AV300", RouteStatus.DRAFT, bogota, medellin));
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

    private RouteEntity createRoute(String flightNumber, RouteStatus status, AirportEntity origin, AirportEntity destination) {
        RouteEntity route = new RouteEntity();
        route.setFlightNumber(flightNumber);
        route.setAirportOrigin(origin);
        route.setAirportDestination(destination);
        route.setDefaultAirplaneType(airplaneType);
        route.setDurationMinutes(60);
        route.setStatus(status);
        return route;
    }

    @DisplayName("Should filter routes by origin IATA code")
    @Test
    void shouldFilterByOriginIataCode() {
        Specification<RouteEntity> spec = RouteSpecifications.hasOriginIataCode("BOG");

        List<RouteEntity> result = routeRepository.findAll(spec);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(RouteEntity::getFlightNumber)
                .containsExactlyInAnyOrder("AV100", "AV300");
    }

    @DisplayName("Should return all routes when origin IATA code is null")
    @Test
    void shouldReturnAllWhenOriginIataCodeIsNull() {
        Specification<RouteEntity> spec = RouteSpecifications.hasOriginIataCode(null);

        List<RouteEntity> result = routeRepository.findAll(spec);

        assertThat(result).hasSize(3);
    }

    @DisplayName("Should filter routes by destination IATA code")
    @Test
    void shouldFilterByDestinationIataCode() {
        Specification<RouteEntity> spec = RouteSpecifications.hasDestinationIataCode("MIA");

        List<RouteEntity> result = routeRepository.findAll(spec);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(RouteEntity::getFlightNumber)
                .containsExactlyInAnyOrder("AV100", "AV200");
    }

    @DisplayName("Should return all routes when destination IATA code is null")
    @Test
    void shouldReturnAllWhenDestinationIataCodeIsNull() {
        Specification<RouteEntity> spec = RouteSpecifications.hasDestinationIataCode(null);

        List<RouteEntity> result = routeRepository.findAll(spec);

        assertThat(result).hasSize(3);
    }

    @DisplayName("Should filter routes by status")
    @Test
    void shouldFilterByStatus() {
        Specification<RouteEntity> spec = RouteSpecifications.hasStatus(RouteStatus.ACTIVE);

        List<RouteEntity> result = routeRepository.findAll(spec);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(RouteEntity::getFlightNumber)
                .containsExactlyInAnyOrder("AV100", "AV200");
    }

    @DisplayName("Should return all routes when status is null")
    @Test
    void shouldReturnAllWhenStatusIsNull() {
        Specification<RouteEntity> spec = RouteSpecifications.hasStatus(null);

        List<RouteEntity> result = routeRepository.findAll(spec);

        assertThat(result).hasSize(3);
    }

    @DisplayName("Should combine multiple specification filters")
    @Test
    void shouldCombineFilters() {
        Specification<RouteEntity> spec = RouteSpecifications.hasOriginIataCode("BOG")
                .and(RouteSpecifications.hasStatus(RouteStatus.ACTIVE));

        List<RouteEntity> result = routeRepository.findAll(spec);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getFlightNumber()).isEqualTo("AV100");
    }

    @DisplayName("Should filter routes by flight number containing text")
    @Test
    void shouldFilterByFlightNumberContains() {
        Specification<RouteEntity> spec = RouteSpecifications.flightNumberContains("AV1");

        List<RouteEntity> result = routeRepository.findAll(spec);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getFlightNumber()).isEqualTo("AV100");
    }

    @DisplayName("Should return all routes when flight number is null")
    @Test
    void shouldReturnAllWhenFlightNumberIsNull() {
        Specification<RouteEntity> spec = RouteSpecifications.flightNumberContains(null);

        List<RouteEntity> result = routeRepository.findAll(spec);

        assertThat(result).hasSize(3);
    }

    @DisplayName("Should return all routes when flight number is blank")
    @Test
    void shouldReturnAllWhenFlightNumberIsBlank() {
        Specification<RouteEntity> spec = RouteSpecifications.flightNumberContains("   ");

        List<RouteEntity> result = routeRepository.findAll(spec);

        assertThat(result).hasSize(3);
    }

    @DisplayName("Should be case-insensitive when filtering by flight number")
    @Test
    void shouldBeCaseInsensitiveForFlightNumber() {
        Specification<RouteEntity> spec = RouteSpecifications.flightNumberContains("av1");

        List<RouteEntity> result = routeRepository.findAll(spec);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getFlightNumber()).isEqualTo("AV100");
    }

    @DisplayName("Should filter routes by airplane type ID")
    @Test
    void shouldFilterByAirplaneTypeId() {
        Specification<RouteEntity> spec = RouteSpecifications.hasAirplaneTypeId(airplaneType.getId());

        List<RouteEntity> result = routeRepository.findAll(spec);

        assertThat(result).hasSize(3);
    }

    @DisplayName("Should return all routes when airplane type ID is null")
    @Test
    void shouldReturnAllWhenAirplaneTypeIdIsNull() {
        Specification<RouteEntity> spec = RouteSpecifications.hasAirplaneTypeId(null);

        List<RouteEntity> result = routeRepository.findAll(spec);

        assertThat(result).hasSize(3);
    }

    @DisplayName("Should return empty when no routes match airplane type ID")
    @Test
    void shouldReturnEmptyWhenNoAirplaneTypeMatch() {
        Specification<RouteEntity> spec = RouteSpecifications.hasAirplaneTypeId(999L);

        List<RouteEntity> result = routeRepository.findAll(spec);

        assertThat(result).isEmpty();
    }

    @DisplayName("Should combine flight number and airplane type filters")
    @Test
    void shouldCombineFlightNumberAndAirplaneType() {
        Specification<RouteEntity> spec = RouteSpecifications.flightNumberContains("AV")
                .and(RouteSpecifications.hasAirplaneTypeId(airplaneType.getId()));

        List<RouteEntity> result = routeRepository.findAll(spec);

        assertThat(result).hasSize(3);
    }

    @DisplayName("Should return empty when no routes match")
    @Test
    void shouldReturnEmptyWhenNoMatch() {
        Specification<RouteEntity> spec = RouteSpecifications.hasOriginIataCode("XYZ");

        List<RouteEntity> result = routeRepository.findAll(spec);

        assertThat(result).isEmpty();
    }
}
