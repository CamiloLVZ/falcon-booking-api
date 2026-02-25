package com.falcon.booking.persistence.repository;

import com.falcon.booking.domain.valueobject.AirplaneTypeStatus;
import com.falcon.booking.domain.valueobject.FlightStatus;
import com.falcon.booking.domain.valueobject.RouteStatus;
import com.falcon.booking.persistence.entity.AirplaneTypeEntity;
import com.falcon.booking.persistence.entity.AirportEntity;
import com.falcon.booking.persistence.entity.CountryEntity;
import com.falcon.booking.persistence.entity.FlightEntity;
import com.falcon.booking.persistence.entity.RouteEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("tests")
class FlightRepositoryTest {

    private int sequence = 0;

    @Autowired
    private FlightRepository flightRepository;
    @Autowired
    private CountryRepository countryRepository;
    @Autowired
    private AirportRepository airportRepository;
    @Autowired
    private AirplaneTypeRepository airplaneTypeRepository;
    @Autowired
    private RouteRepository routeRepository;

    private CountryEntity createCountry(String isoCode, String name) {
        CountryEntity country = new CountryEntity();
        country.setIsoCode(isoCode);
        country.setName(name);
        return country;
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

    private AirplaneTypeEntity createAirplaneType(int suffix) {
        AirplaneTypeEntity airplaneType = new AirplaneTypeEntity();
        airplaneType.setProducer("Airbus " + suffix);
        airplaneType.setModel("A320-" + suffix);
        airplaneType.setEconomySeats(100);
        airplaneType.setFirstClassSeats(10);
        airplaneType.setStatus(AirplaneTypeStatus.ACTIVE);
        return airplaneType;
    }

    private RouteEntity createRoute(String flightNumber) {
        sequence++;
        CountryEntity country = countryRepository.save(createCountry("C" + sequence, "Country " + sequence));
        AirportEntity origin = airportRepository.save(createAirport("B" + sequence + "G", country));
        AirportEntity destination = airportRepository.save(createAirport("M" + sequence + "E", country));
        AirplaneTypeEntity airplaneType = airplaneTypeRepository.save(createAirplaneType(sequence));

        RouteEntity route = new RouteEntity();
        route.setFlightNumber(flightNumber);
        route.setAirportOrigin(origin);
        route.setAirportDestination(destination);
        route.setDefaultAirplaneType(airplaneType);
        route.setLengthMinutes(60);
        route.setStatus(RouteStatus.ACTIVE);
        return routeRepository.save(route);
    }

    private FlightEntity createFlight(RouteEntity route, OffsetDateTime departureDateTime, FlightStatus status) {
        FlightEntity flightEntity = new FlightEntity();
        flightEntity.setRoute(route);
        flightEntity.setAirplaneType(route.getDefaultAirplaneType());
        flightEntity.setDepartureDateTime(departureDateTime);
        flightEntity.setStatus(status);
        return flightEntity;
    }

    @DisplayName("Should return true when flight exists by route and departure date")
    @Test
    void shouldReturnTrue_existsByRouteAndDepartureDateTime() {
        RouteEntity route = createRoute("AV1234");
        OffsetDateTime departure = OffsetDateTime.now().plusDays(1).withNano(0);
        flightRepository.save(createFlight(route, departure, FlightStatus.SCHEDULED));

        boolean exists = flightRepository.existsByRouteAndDepartureDateTime(route, departure);

        assertThat(exists).isTrue();
    }

    @DisplayName("Should return flights in route and departure range")
    @Test
    void shouldReturnFlights_findAllByRouteAndDepartureDateTimeBetween() {
        RouteEntity route = createRoute("AV1234");
        OffsetDateTime date1 = OffsetDateTime.now().plusDays(1).withHour(8).withMinute(0).withSecond(0).withNano(0);
        OffsetDateTime date2 = date1.plusDays(1);
        OffsetDateTime date3 = date1.plusDays(5);

        flightRepository.save(createFlight(route, date1, FlightStatus.SCHEDULED));
        flightRepository.save(createFlight(route, date2, FlightStatus.CHECK_IN_AVAILABLE));
        flightRepository.save(createFlight(route, date3, FlightStatus.SCHEDULED));

        List<FlightEntity> flights = flightRepository.findAllByRouteAndDepartureDateTimeBetween(route, date1.minusHours(1), date2.plusHours(1));

        assertThat(flights).hasSize(2);
    }

    @DisplayName("Should return flights excluding canceled and completed status")
    @Test
    void shouldReturnFlights_findAllByStatusNotAndStatusNot() {
        RouteEntity route = createRoute("AV1234");
        OffsetDateTime departure = OffsetDateTime.now().plusDays(2).withNano(0);

        flightRepository.save(createFlight(route, departure.plusHours(1), FlightStatus.SCHEDULED));
        flightRepository.save(createFlight(route, departure.plusHours(2), FlightStatus.CANCELED));
        flightRepository.save(createFlight(route, departure.plusHours(3), FlightStatus.COMPLETED));

        List<FlightEntity> flights = flightRepository.findAllByStatusNotAndStatusNot(FlightStatus.CANCELED, FlightStatus.COMPLETED);

        assertThat(flights).hasSize(1);
        assertThat(flights.get(0).getStatus()).isEqualTo(FlightStatus.SCHEDULED);
    }

    @DisplayName("Should return existing departure times by route")
    @Test
    void shouldReturnDepartureTimes_findExistingDepartureTimes() {
        RouteEntity route = createRoute("AV1234");
        OffsetDateTime departure1 = OffsetDateTime.now().plusDays(2).withNano(0);
        OffsetDateTime departure2 = departure1.plusHours(2);
        OffsetDateTime departure3 = departure1.plusHours(4);

        flightRepository.save(createFlight(route, departure1, FlightStatus.SCHEDULED));
        flightRepository.save(createFlight(route, departure2, FlightStatus.SCHEDULED));

        List<OffsetDateTime> existing = flightRepository.findExistingDepartureTimes(route, List.of(departure1, departure3));

        assertThat(existing).containsExactly(departure1);
    }
}
