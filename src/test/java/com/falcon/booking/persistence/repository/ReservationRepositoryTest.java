package com.falcon.booking.persistence.repository;

import com.falcon.booking.common.enums.AirplaneTypeStatus;
import com.falcon.booking.common.enums.FlightStatus;
import com.falcon.booking.common.enums.ReservationStatus;
import com.falcon.booking.common.enums.RouteStatus;
import com.falcon.booking.persistence.entity.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import org.springframework.test.util.ReflectionTestUtils;


import java.time.Instant;
import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class ReservationRepositoryTest extends BaseRepositoryTest {

    private int sequence = 0;

    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private CountryRepository countryRepository;
    @Autowired
    private AirportRepository airportRepository;
    @Autowired
    private AirplaneTypeRepository airplaneTypeRepository;
    @Autowired
    private RouteRepository routeRepository;
    @Autowired
    private FlightRepository flightRepository;

    private CountryEntity createCountry() {
        sequence++;
        CountryEntity country = new CountryEntity();
        country.setIsoCode("C" + sequence);
        country.setName("Country " + sequence);
        return countryRepository.save(country);
    }

    private AirportEntity createAirport(String iataCode, CountryEntity country) {
        AirportEntity airport = new AirportEntity();
        airport.setIataCode(iataCode);
        airport.setName("Airport " + iataCode);
        airport.setCity("City " + iataCode);
        airport.setCountry(country);
        airport.setTimezone("America/Bogota");
        return airportRepository.save(airport);
    }

    private AirplaneTypeEntity createAirplaneType() {
        AirplaneTypeEntity airplaneType = new AirplaneTypeEntity();
        airplaneType.setProducer("Airbus");
        airplaneType.setModel("A320-" + sequence);
        airplaneType.configureSeats(108, 12, "ABCDEF");
        airplaneType.setStatus(AirplaneTypeStatus.ACTIVE);
        return airplaneTypeRepository.save(airplaneType);
    }

    private FlightEntity createFlight(String flightNumber) {
        CountryEntity country = createCountry();
        AirportEntity origin = createAirport("B" + sequence + "G", country);
        AirportEntity destination = createAirport("M" + sequence + "E", country);
        AirplaneTypeEntity airplaneType = createAirplaneType();

        RouteEntity route = new RouteEntity();
        route.setFlightNumber(flightNumber);
        route.setAirportOrigin(origin);
        route.setAirportDestination(destination);
        route.setDefaultAirplaneType(airplaneType);
        route.setDurationMinutes(60);
        route.setStatus(RouteStatus.ACTIVE);
        route = routeRepository.save(route);

        FlightEntity flight = new FlightEntity();
        flight.setRoute(route);
        flight.setAirplaneType(airplaneType);
        flight.setDepartureDateTime(OffsetDateTime.now().plusDays(1).withNano(0));
        flight.setStatus(FlightStatus.SCHEDULED);
        return flightRepository.save(flight);
    }

    @DisplayName("Should return true when reservation number exists")
    @Test
    void shouldReturnTrue_existsByNumber() {
        FlightEntity flight = createFlight("AV1234");
        reservationRepository.save(new ReservationEntity("ABC123", flight, "test@mail.com", Instant.now()));

        boolean exists = reservationRepository.existsByNumber("ABC123");

        assertThat(exists).isTrue();
    }

    @DisplayName("Should return reservation by number")
    @Test
    void shouldReturnReservation_findByNumber() {
        FlightEntity flight = createFlight("AV4321");
        reservationRepository.save(new ReservationEntity("ZZZ999", flight, "test@mail.com", Instant.now()));

        ReservationEntity reservation = reservationRepository.findByNumber("ZZZ999").orElse(null);

        assertThat(reservation).isNotNull();
        assertThat(reservation.getContactEmail()).isEqualTo("test@mail.com");
    }

    @DisplayName("Should return reservations by flight and status")
    @Test
    void shouldReturnReservations_findAllByFlightAndStatus() {
        FlightEntity flight = createFlight("AV5555");
        ReservationEntity reserved = reservationRepository.save(new ReservationEntity("AAA111", flight, "one@mail.com", Instant.now()));
        ReservationEntity canceled = reservationRepository.save(new ReservationEntity("AAA222", flight, "two@mail.com", Instant.now()));
        ReflectionTestUtils.setField(canceled, "status", ReservationStatus.CANCELED);
        reservationRepository.save(canceled);

        Page<ReservationEntity> reservations = reservationRepository.findAllByFlightAndStatus(
                flight,
                ReservationStatus.RESERVED,
                PageRequest.of(0, 10, Sort.by("reservationDatetime").ascending())
        );

        assertThat(reservations.getContent()).hasSize(1);
        assertThat(reservations.getContent().get(0).getNumber()).isEqualTo(reserved.getNumber());
        assertThat(reservations.getTotalElements()).isEqualTo(1);
    }
}
