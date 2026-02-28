package com.falcon.booking.domain.service;

import com.falcon.booking.domain.exception.Flight.FlightCanNotBeReservedException;
import com.falcon.booking.domain.exception.Reservation.DuplicateSeatNumberInReservationException;
import com.falcon.booking.domain.exception.Reservation.ReservationMustHavePassengersException;
import com.falcon.booking.domain.exception.Reservation.ReservationNotFoundException;
import com.falcon.booking.domain.exception.Reservation.SeatNumberAlreadyTakenException;
import com.falcon.booking.domain.exception.Reservation.SeatNumberOutOfRangeException;
import com.falcon.booking.domain.mapper.FlightMapper;
import com.falcon.booking.domain.mapper.PassengerReservationMapper;
import com.falcon.booking.domain.mapper.ReservationMapper;
import com.falcon.booking.domain.valueobject.AirplaneTypeStatus;
import com.falcon.booking.domain.valueobject.FlightStatus;
import com.falcon.booking.domain.valueobject.PassengerGender;
import com.falcon.booking.domain.valueobject.PassengerReservationStatus;
import com.falcon.booking.domain.valueobject.ReservationStatus;
import com.falcon.booking.domain.valueobject.RouteStatus;
import com.falcon.booking.persistence.entity.AirplaneTypeEntity;
import com.falcon.booking.persistence.entity.AirportEntity;
import com.falcon.booking.persistence.entity.CountryEntity;
import com.falcon.booking.persistence.entity.FlightEntity;
import com.falcon.booking.persistence.entity.PassengerEntity;
import com.falcon.booking.persistence.entity.PassengerReservationEntity;
import com.falcon.booking.persistence.entity.ReservationEntity;
import com.falcon.booking.persistence.entity.RouteEntity;
import com.falcon.booking.persistence.repository.PassengerReservationRepository;
import com.falcon.booking.persistence.repository.ReservationRepository;
import com.falcon.booking.web.dto.passenger.AddPassengerDto;
import com.falcon.booking.web.dto.reservation.AddPassengerReservationDto;
import com.falcon.booking.web.dto.reservation.AddReservationDto;
import com.falcon.booking.web.dto.reservation.ResponsePassengerReservationDto;
import com.falcon.booking.web.dto.reservation.ResponseReservationDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;
    @Mock
    private PassengerReservationRepository passengerReservationRepository;
    @Mock
    private FlightService flightService;
    @Mock
    private PassengerService passengerService;
    @Mock
    private FlightMapper flightMapper;
    @Mock
    private PassengerReservationMapper passengerReservationMapper;
    @Mock
    private ReservationMapper reservationMapper;

    @InjectMocks
    private ReservationService reservationService;

    private CountryEntity createCountry(String isoCode) {
        CountryEntity country = new CountryEntity();
        country.setIsoCode(isoCode);
        country.setName("Country " + isoCode);
        return country;
    }

    private FlightEntity createFlight(FlightStatus status, int economySeats, int firstClassSeats) {
        CountryEntity country = createCountry("CO");

        AirportEntity origin = new AirportEntity();
        origin.setIataCode("BOG");
        origin.setName("El Dorado");
        origin.setCity("Bogota");
        origin.setCountry(country);
        origin.setTimezone("America/Bogota");

        AirportEntity destination = new AirportEntity();
        destination.setIataCode("MDE");
        destination.setName("JMC");
        destination.setCity("Medellin");
        destination.setCountry(country);
        destination.setTimezone("America/Bogota");

        AirplaneTypeEntity airplaneType = new AirplaneTypeEntity();
        airplaneType.setProducer("Airbus");
        airplaneType.setModel("A320");
        airplaneType.setEconomySeats(economySeats);
        airplaneType.setFirstClassSeats(firstClassSeats);
        airplaneType.setStatus(AirplaneTypeStatus.ACTIVE);

        RouteEntity route = new RouteEntity();
        route.setFlightNumber("AV1234");
        route.setAirportOrigin(origin);
        route.setAirportDestination(destination);
        route.setDefaultAirplaneType(airplaneType);
        route.setLengthMinutes(60);
        route.setStatus(RouteStatus.ACTIVE);

        FlightEntity flight = new FlightEntity();
        flight.setId(5L);
        flight.setRoute(route);
        flight.setAirplaneType(airplaneType);
        flight.setDepartureDateTime(OffsetDateTime.now().plusDays(1));
        flight.setStatus(status);
        return flight;
    }

    private PassengerEntity createPassenger(String identificationNumber) {
        PassengerEntity passenger = new PassengerEntity();
        passenger.setFirstName("Ana");
        passenger.setLastName("Perez");
        passenger.setGender(PassengerGender.F);
        passenger.setCountryNationality(createCountry("CO"));
        passenger.setDateOfBirth(LocalDate.now().minusYears(20));
        passenger.setPassportNumber("P" + identificationNumber);
        passenger.setIdentificationNumber(identificationNumber);
        return passenger;
    }

    @DisplayName("Should return reservation entity by number")
    @Test
    void shouldReturnEntity_getReservationEntityByNumber() {
        FlightEntity flight = createFlight(FlightStatus.SCHEDULED, 100, 10);
        ReservationEntity reservation = new ReservationEntity("ABC123", flight, "mail@test.com", Instant.now());
        given(reservationRepository.findByNumber("ABC123")).willReturn(Optional.of(reservation));

        ReservationEntity result = reservationService.getReservationEntityByNumber(" abc123 ");

        assertThat(result).isEqualTo(reservation);
        verify(reservationRepository).findByNumber("ABC123");
    }

    @DisplayName("Should throw exception when reservation does not exist")
    @Test
    void shouldThrowException_getReservationEntityByNumber() {
        given(reservationRepository.findByNumber("ABC123")).willReturn(Optional.empty());

        ReservationNotFoundException ex = assertThrows(ReservationNotFoundException.class,
                () -> reservationService.getReservationEntityByNumber("abc123"));

        assertThat(ex.getMessage()).contains("ABC123");
    }

    @DisplayName("Should add reservation when data is valid")
    @Test
    void shouldAddReservation_addReservation() {
        FlightEntity flight = createFlight(FlightStatus.SCHEDULED, 100, 10);
        AddPassengerDto addPassengerDto = new AddPassengerDto("Ana", "Perez", PassengerGender.F,
                "CO", LocalDate.now().minusYears(20), "P123", "123");
        AddReservationDto request = new AddReservationDto(5L, "contact@test.com",
                List.of(new AddPassengerReservationDto(addPassengerDto, 8)));

        ReservationEntity savedReservation = new ReservationEntity("ABC123", flight, "contact@test.com", Instant.now());
        PassengerEntity passenger = createPassenger("123");
        PassengerReservationEntity savedPassengerReservation = new PassengerReservationEntity(passenger, savedReservation, 8);

        given(flightService.getFlightEntity(5L)).willReturn(flight);
        given(reservationRepository.existsByNumber(any())).willReturn(false);
        given(reservationRepository.save(any(ReservationEntity.class))).willReturn(savedReservation);
        given(passengerReservationRepository.findAllBySeatNumberAndFlight(8, flight)).willReturn(List.of());
        given(passengerService.createOrGetPassenger(addPassengerDto)).willReturn(passenger);
        given(passengerReservationRepository.saveAll(any())).willReturn(List.of(savedPassengerReservation));
        given(passengerReservationMapper.toResponseDto(List.of(savedPassengerReservation)))
                .willReturn(List.of(new ResponsePassengerReservationDto(null, 8, PassengerReservationStatus.RESERVED)));

        ResponseReservationDto result = reservationService.addReservation(request);

        assertThat(result.number()).isEqualTo("ABC123");
        verify(passengerReservationRepository).saveAll(any());
    }

    @DisplayName("Should throw exception when flight can not be reserved")
    @Test
    void shouldThrowException_addReservation() {
        FlightEntity flight = createFlight(FlightStatus.CANCELED, 100, 10);
        AddReservationDto request = new AddReservationDto(5L, "contact@test.com", List.of());
        given(flightService.getFlightEntity(5L)).willReturn(flight);

        assertThrows(FlightCanNotBeReservedException.class, () -> reservationService.addReservation(request));
        verify(reservationRepository, never()).save(any());
    }

    @DisplayName("Should throw exception when reservation has no passengers")
    @Test
    void shouldThrowExceptionNoPassengers_addReservation() {
        FlightEntity flight = createFlight(FlightStatus.SCHEDULED, 100, 10);
        AddReservationDto request = new AddReservationDto(5L, "contact@test.com", List.of());

        given(flightService.getFlightEntity(5L)).willReturn(flight);
        given(reservationRepository.existsByNumber(any())).willReturn(false);
        given(reservationRepository.save(any(ReservationEntity.class)))
                .willReturn(new ReservationEntity("ABC123", flight, "contact@test.com", Instant.now()));

        assertThrows(ReservationMustHavePassengersException.class, () -> reservationService.addReservation(request));
    }

    @DisplayName("Should throw exception when seat number is duplicated in request")
    @Test
    void shouldThrowExceptionDuplicateSeat_addReservation() {
        FlightEntity flight = createFlight(FlightStatus.SCHEDULED, 100, 10);
        AddPassengerDto passenger1 = new AddPassengerDto("Ana", "Perez", PassengerGender.F,
                "CO", LocalDate.now().minusYears(20), "P123", "123");
        AddPassengerDto passenger2 = new AddPassengerDto("Luis", "Gomez", PassengerGender.M,
                "CO", LocalDate.now().minusYears(25), "P124", "124");

        AddReservationDto request = new AddReservationDto(5L, "contact@test.com", List.of(
                new AddPassengerReservationDto(passenger1, 8),
                new AddPassengerReservationDto(passenger2, 8)
        ));

        given(flightService.getFlightEntity(5L)).willReturn(flight);
        given(reservationRepository.existsByNumber(any())).willReturn(false);
        given(reservationRepository.save(any(ReservationEntity.class)))
                .willReturn(new ReservationEntity("ABC123", flight, "contact@test.com", Instant.now()));
        given(passengerReservationRepository.findAllBySeatNumberAndFlight(8, flight)).willReturn(List.of());
        given(passengerService.createOrGetPassenger(passenger1)).willReturn(createPassenger("123"));

        assertThrows(DuplicateSeatNumberInReservationException.class, () -> reservationService.addReservation(request));
    }

    @DisplayName("Should throw exception when seat number is out of range")
    @Test
    void shouldThrowExceptionOutOfRangeSeat_addReservation() {
        FlightEntity flight = createFlight(FlightStatus.SCHEDULED, 10, 0);
        AddPassengerDto passenger = new AddPassengerDto("Ana", "Perez", PassengerGender.F,
                "CO", LocalDate.now().minusYears(20), "P123", "123");
        AddReservationDto request = new AddReservationDto(5L, "contact@test.com",
                List.of(new AddPassengerReservationDto(passenger, 20)));

        given(flightService.getFlightEntity(5L)).willReturn(flight);
        given(reservationRepository.existsByNumber(any())).willReturn(false);
        given(reservationRepository.save(any(ReservationEntity.class)))
                .willReturn(new ReservationEntity("ABC123", flight, "contact@test.com", Instant.now()));

        assertThrows(SeatNumberOutOfRangeException.class, () -> reservationService.addReservation(request));
    }

    @DisplayName("Should throw exception when seat is already taken")
    @Test
    void shouldThrowExceptionSeatTaken_addReservation() {
        FlightEntity flight = createFlight(FlightStatus.SCHEDULED, 100, 10);
        AddPassengerDto passenger = new AddPassengerDto("Ana", "Perez", PassengerGender.F,
                "CO", LocalDate.now().minusYears(20), "P123", "123");
        AddReservationDto request = new AddReservationDto(5L, "contact@test.com",
                List.of(new AddPassengerReservationDto(passenger, 8)));

        ReservationEntity reservation = new ReservationEntity("ABC123", flight, "contact@test.com", Instant.now());
        PassengerReservationEntity existingReservation = new PassengerReservationEntity(createPassenger("999"), reservation, 8);

        given(flightService.getFlightEntity(5L)).willReturn(flight);
        given(reservationRepository.existsByNumber(any())).willReturn(false);
        given(reservationRepository.save(any(ReservationEntity.class))).willReturn(reservation);
        given(passengerReservationRepository.findAllBySeatNumberAndFlight(8, flight))
                .willReturn(List.of(existingReservation));

        assertThrows(SeatNumberAlreadyTakenException.class, () -> reservationService.addReservation(request));
    }

    @DisplayName("Should return active reservation entities by flight")
    @Test
    void shouldReturnEntities_getAllReservationEntitiesActiveByFlight() {
        FlightEntity flight = createFlight(FlightStatus.SCHEDULED, 100, 10);
        ReservationEntity reservation = new ReservationEntity("ABC123", flight, "contact@test.com", Instant.now());

        given(reservationRepository.findAllByFlightAndStatus(flight, ReservationStatus.RESERVED))
                .willReturn(List.of(reservation));

        List<ReservationEntity> result = reservationService.getAllReservationEntitiesActiveByFlight(flight);

        assertThat(result).hasSize(1);
    }
}
