package com.falcon.booking.persistence.specification;

import com.falcon.booking.common.enums.AirplaneTypeStatus;
import com.falcon.booking.common.enums.FlightStatus;
import com.falcon.booking.common.enums.RouteStatus;
import com.falcon.booking.persistence.entity.*;
import com.falcon.booking.persistence.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;

import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FlightSpecificationsIT extends BaseRepositoryTest {

    @Autowired
    private FlightRepository flightRepository;
    @Autowired
    private RouteRepository routeRepository;
    @Autowired
    private CountryRepository countryRepository;
    @Autowired
    private AirportRepository airportRepository;
    @Autowired
    private AirplaneTypeRepository airplaneTypeRepository;

    private RouteEntity routeBogMia;
    private RouteEntity routeMdeMia;
    private OffsetDateTime baseTime;

    @BeforeEach
    void setUp() {
        CountryEntity country = countryRepository.findByIsoCode("CO")
                .orElseGet(() -> countryRepository.save(createCountry("CO", "Colombia")));
        CountryEntity usa = countryRepository.findByIsoCode("US")
                .orElseGet(() -> countryRepository.save(createCountry("US", "United States")));

        AirportEntity bogota = airportRepository.findByIataCode("BOG")
                .orElseGet(() -> airportRepository.save(createAirport("BOG", country)));
        AirportEntity medellin = airportRepository.findByIataCode("MDE")
                .orElseGet(() -> airportRepository.save(createAirport("MDE", country)));
        AirportEntity miami = airportRepository.findByIataCode("MIA")
                .orElseGet(() -> airportRepository.save(createAirport("MIA", usa)));

        AirplaneTypeEntity airplaneType = new AirplaneTypeEntity();
        airplaneType.setProducer("Airbus");
        airplaneType.setModel("A320");
        airplaneType.configureSeats(108, 12, "ABCDEF");
        airplaneType.setStatus(AirplaneTypeStatus.ACTIVE);
        airplaneType = airplaneTypeRepository.save(airplaneType);

        routeBogMia = routeRepository.save(createRoute("AV100", RouteStatus.ACTIVE, bogota, miami, airplaneType));
        routeMdeMia = routeRepository.save(createRoute("AV200", RouteStatus.ACTIVE, medellin, miami, airplaneType));

        baseTime = OffsetDateTime.now().plusDays(10).withHour(10).withMinute(0).withSecond(0).withNano(0);

        flightRepository.save(createFlight(routeBogMia, baseTime, FlightStatus.SCHEDULED));
        flightRepository.save(createFlight(routeBogMia, baseTime.plusDays(1), FlightStatus.CANCELED));
        flightRepository.save(createFlight(routeBogMia, baseTime.plusDays(2), FlightStatus.SCHEDULED));
        flightRepository.save(createFlight(routeMdeMia, baseTime, FlightStatus.SCHEDULED));
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

    private RouteEntity createRoute(String flightNumber, RouteStatus status, AirportEntity origin, AirportEntity destination, AirplaneTypeEntity airplaneType) {
        RouteEntity route = new RouteEntity();
        route.setFlightNumber(flightNumber);
        route.setAirportOrigin(origin);
        route.setAirportDestination(destination);
        route.setDefaultAirplaneType(airplaneType);
        route.setDurationMinutes(60);
        route.setStatus(status);
        return route;
    }

    private FlightEntity createFlight(RouteEntity route, OffsetDateTime departureDateTime, FlightStatus status) {
        FlightEntity flight = new FlightEntity();
        flight.setRoute(route);
        flight.setAirplaneType(route.getDefaultAirplaneType());
        flight.setDepartureDateTime(departureDateTime);
        flight.setStatus(status);
        return flight;
    }

    @DisplayName("Should filter flights by route")
    @Test
    void shouldFilterByRoute() {
        Specification<FlightEntity> spec = FlightSpecifications.hasRoute(routeBogMia);

        List<FlightEntity> result = flightRepository.findAll(spec);

        assertThat(result).hasSize(3);
        result.forEach(f -> assertThat(f.getRoute()).isEqualTo(routeBogMia));
    }

    @DisplayName("Should return all flights when route is null")
    @Test
    void shouldReturnAllWhenRouteIsNull() {
        Specification<FlightEntity> spec = FlightSpecifications.hasRoute(null);

        List<FlightEntity> result = flightRepository.findAll(spec);

        assertThat(result).hasSize(4);
    }

    @DisplayName("Should filter flights by flight number (via route)")
    @Test
    void shouldFilterByFlightNumber() {
        Specification<FlightEntity> spec = FlightSpecifications.hasFlightNumber("AV100");

        List<FlightEntity> result = flightRepository.findAll(spec);

        assertThat(result).hasSize(3);
    }

    @DisplayName("Should return all flights when flight number is null")
    @Test
    void shouldReturnAllWhenFlightNumberIsNull() {
        Specification<FlightEntity> spec = FlightSpecifications.hasFlightNumber(null);

        List<FlightEntity> result = flightRepository.findAll(spec);

        assertThat(result).hasSize(4);
    }

    @DisplayName("Should return all flights when flight number is blank")
    @Test
    void shouldReturnAllWhenFlightNumberIsBlank() {
        Specification<FlightEntity> spec = FlightSpecifications.hasFlightNumber("   ");

        List<FlightEntity> result = flightRepository.findAll(spec);

        assertThat(result).hasSize(4);
    }

    @DisplayName("Should filter flights by status")
    @Test
    void shouldFilterByStatus() {
        Specification<FlightEntity> spec = FlightSpecifications.hasStatus(FlightStatus.SCHEDULED);

        List<FlightEntity> result = flightRepository.findAll(spec);

        assertThat(result).hasSize(3);
    }

    @DisplayName("Should return all flights when status is null")
    @Test
    void shouldReturnAllWhenStatusIsNull() {
        Specification<FlightEntity> spec = FlightSpecifications.hasStatus(null);

        List<FlightEntity> result = flightRepository.findAll(spec);

        assertThat(result).hasSize(4);
    }

    @DisplayName("Should filter flights by start date")
    @Test
    void shouldFilterByDateStart() {
        Specification<FlightEntity> spec = FlightSpecifications.hasDateStart(baseTime.plusDays(1));

        List<FlightEntity> result = flightRepository.findAll(spec);

        assertThat(result).hasSize(2);
        result.forEach(f -> assertThat(f.getDepartureDateTime())
                .isAfterOrEqualTo(baseTime.plusDays(1)));
    }

    @DisplayName("Should return null predicate when date start is null, returning all flights")
    @Test
    void shouldReturnAllWhenDateStartIsNull() {
        Specification<FlightEntity> spec = FlightSpecifications.hasDateStart(null);

        List<FlightEntity> result = flightRepository.findAll(spec);

        assertThat(result).hasSize(4);
    }

    @DisplayName("Should filter flights by end date")
    @Test
    void shouldFilterByDateEnd() {
        Specification<FlightEntity> spec = FlightSpecifications.hasDateEnd(baseTime);

        List<FlightEntity> result = flightRepository.findAll(spec);

        assertThat(result).hasSize(2);
        result.forEach(f -> assertThat(f.getDepartureDateTime())
                .isBeforeOrEqualTo(baseTime));
    }

    @DisplayName("Should return null predicate when date end is null, returning all flights")
    @Test
    void shouldReturnAllWhenDateEndIsNull() {
        Specification<FlightEntity> spec = FlightSpecifications.hasDateEnd(null);

        List<FlightEntity> result = flightRepository.findAll(spec);

        assertThat(result).hasSize(4);
    }

    @DisplayName("Should combine date range filters")
    @Test
    void shouldCombineDateRange() {
        Specification<FlightEntity> spec = FlightSpecifications.hasDateStart(baseTime)
                .and(FlightSpecifications.hasDateEnd(baseTime.plusDays(1)));

        List<FlightEntity> result = flightRepository.findAll(spec);

        assertThat(result).hasSize(3);
    }

    @DisplayName("Should combine multiple filters")
    @Test
    void shouldCombineMultipleFilters() {
        Specification<FlightEntity> spec = FlightSpecifications.hasRoute(routeBogMia)
                .and(FlightSpecifications.hasStatus(FlightStatus.SCHEDULED));

        List<FlightEntity> result = flightRepository.findAll(spec);

        assertThat(result).hasSize(2);
    }
}
