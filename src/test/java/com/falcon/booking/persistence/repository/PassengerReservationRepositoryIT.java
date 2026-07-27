package com.falcon.booking.persistence.repository;

import com.falcon.booking.common.enums.*;
import com.falcon.booking.persistence.entity.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;


import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PassengerReservationRepositoryIT extends BaseRepositoryTest {

    private int sequence = 0;

    @Autowired
    private PassengerReservationRepository passengerReservationRepository;
    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private PassengerRepository passengerRepository;
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

    private FlightEntity createFlight(String flightNumber) {
        CountryEntity country = createCountry();

        AirportEntity origin = new AirportEntity();
        origin.setIataCode("B" + sequence + "G");
        origin.setName("Airport B" + sequence + "G");
        origin.setCity("City B" + sequence + "G");
        origin.setCountry(country);
        origin.setTimezone("America/Bogota");
        origin = airportRepository.save(origin);

        AirportEntity destination = new AirportEntity();
        destination.setIataCode("M" + sequence + "E");
        destination.setName("Airport M" + sequence + "E");
        destination.setCity("City M" + sequence + "E");
        destination.setCountry(country);
        destination.setTimezone("America/Bogota");
        destination = airportRepository.save(destination);

        AirplaneTypeEntity airplaneType = new AirplaneTypeEntity();
        airplaneType.setProducer("Airbus");
        airplaneType.setModel("A320-" + sequence);
        airplaneType.configureSeats(108, 12, "ABCDEF");
        airplaneType.setStatus(AirplaneTypeStatus.ACTIVE);
        airplaneType = airplaneTypeRepository.save(airplaneType);

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

    private PassengerEntity createPassenger(String identificationNumber, CountryEntity country) {
        PassengerEntity passenger = new PassengerEntity();
        passenger.setFirstName("Name " + identificationNumber);
        passenger.setLastName("Last " + identificationNumber);
        passenger.setGender(PassengerGender.F);
        passenger.setCountryNationality(country);
        passenger.setDateOfBirth(LocalDate.now().minusYears(30));
        passenger.setPassportNumber("P" + identificationNumber);
        passenger.setIdentificationNumber(identificationNumber);
        return passengerRepository.save(passenger);
    }

    @DisplayName("Should return passenger reservations by seat number and flight")
    @Test
    void shouldReturnReservations_findAllBySeatNumberAndFlight() {
        FlightEntity flight = createFlight("AV1234");
        CountryEntity country = countryRepository.findAllByOrderByNameAsc().get(0);

        ReservationEntity reservation = reservationRepository.save(
                new ReservationEntity("ABC123", flight, "test@mail.com", Instant.now()));

        PassengerEntity passenger = createPassenger("1001", country);
        PassengerReservationEntity passengerReservation = new PassengerReservationEntity(passenger, reservation, 12, SeatClass.ECONOMY);
        passengerReservationRepository.save(passengerReservation);

        List<PassengerReservationEntity> result = passengerReservationRepository.findAllBySeatNumberAndFlight(12, flight);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getPassenger().getIdentificationNumber()).isEqualTo("1001");
    }

    @DisplayName("Should return passenger reservations by passenger")
    @Test
    void shouldReturnReservations_findAllByPassenger() {
        FlightEntity flight = createFlight("AV2222");
        CountryEntity country = countryRepository.findAllByOrderByNameAsc().get(0);

        ReservationEntity reservationOne = reservationRepository.save(
                new ReservationEntity("ONE111", flight, "one@mail.com", Instant.now()));
        ReservationEntity reservationTwo = reservationRepository.save(
                new ReservationEntity("TWO222", flight, "two@mail.com", Instant.now()));

        PassengerEntity passenger = createPassenger("2002", country);

        passengerReservationRepository.save(new PassengerReservationEntity(passenger, reservationOne, 5, SeatClass.ECONOMY));
        passengerReservationRepository.save(new PassengerReservationEntity(passenger, reservationTwo, 6, SeatClass.ECONOMY));

        Page<PassengerReservationEntity> result = passengerReservationRepository.findAllByPassenger(passenger, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(2);
    }

    @DisplayName("Should return reservations by flight, passenger and status not")
    @Test
    void shouldReturnReservations_findAllByFlightAndPassengerAndStatusNot() {
        FlightEntity flight = createFlight("AV3333");
        CountryEntity country = countryRepository.findAllByOrderByNameAsc().get(0);

        ReservationEntity reservationOne = reservationRepository.save(
                new ReservationEntity("ONE333", flight, "one@mail.com", Instant.now()));
        ReservationEntity reservationTwo = reservationRepository.save(
                new ReservationEntity("TWO333", flight, "two@mail.com", Instant.now()));

        PassengerEntity passenger = createPassenger("3003", country);

        PassengerReservationEntity pr1 = new PassengerReservationEntity(passenger, reservationOne, 5, com.falcon.booking.common.enums.SeatClass.ECONOMY);

        PassengerReservationEntity pr2 = new PassengerReservationEntity(passenger, reservationTwo, 6, com.falcon.booking.common.enums.SeatClass.ECONOMY);
        pr2.cancel();
        passengerReservationRepository.save(pr1);
        passengerReservationRepository.save(pr2);

        List<PassengerReservationEntity> result = passengerReservationRepository.findAllByFlightAndPassengerAndStatusNot(
                flight, passenger, com.falcon.booking.common.enums.PassengerReservationStatus.CANCELED);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getReservation().getNumber()).isEqualTo("ONE333");
    }
}
