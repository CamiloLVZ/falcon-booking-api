package com.falcon.booking.feature.reservation.service;

import com.falcon.booking.common.enums.*;
import com.falcon.booking.feature.flight.exception.FlightCanNotBeReservedException;
import com.falcon.booking.feature.flight.service.FlightQueryService;
import com.falcon.booking.feature.passenger.dto.AddPassengerDto;
import com.falcon.booking.feature.passenger.service.PassengerService;
import com.falcon.booking.feature.reservation.dto.AddPassengerReservationDto;
import com.falcon.booking.feature.reservation.dto.AddReservationDto;
import com.falcon.booking.feature.reservation.dto.ResponsePassengerReservationDto;
import com.falcon.booking.feature.reservation.dto.ResponseReservationDto;
import com.falcon.booking.feature.reservation.exception.FlightCapacityExceededException;
import com.falcon.booking.feature.reservation.exception.ReservationMustHavePassengersException;
import com.falcon.booking.feature.reservation.mapper.PassengerReservationMapper;
import com.falcon.booking.persistence.entity.*;
import com.falcon.booking.persistence.repository.PassengerReservationRepository;
import com.falcon.booking.persistence.repository.ReservationRepository;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ReservationCommandServiceTest {

    @Mock
    private ReservationRepository reservationRepository;
    @Mock
    private PassengerReservationRepository passengerReservationRepository;
    @Mock
    private FlightQueryService flightQueryService;
    @Mock
    private PassengerService passengerService;
    @Mock
    private com.falcon.booking.feature.flight.mapper.FlightMapper flightMapper;
    @Mock
    private PassengerReservationMapper passengerReservationMapper;
    @Mock
    private com.falcon.booking.feature.reservation.component.ReservationNumberGenerator reservationNumberGenerator;

    @InjectMocks
    private ReservationCommandService reservationCommandService;

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
        route.setDurationMinutes(60);
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

    @DisplayName("Should add reservation when data is valid")
    @Test
    void shouldAddReservation_addReservation() {
        FlightEntity flight = createFlight(FlightStatus.SCHEDULED, 100, 10);
        AddPassengerDto addPassengerDto = new AddPassengerDto("Ana", "Perez", PassengerGender.F,
                "CO", LocalDate.now().minusYears(20), "P123", "123");
        AddReservationDto request = new AddReservationDto(5L, "contact@test.com",
                List.of(new AddPassengerReservationDto(addPassengerDto, null, SeatClass.ECONOMY)));

        ReservationEntity savedReservation = new ReservationEntity("ABC123", flight, "contact@test.com", Instant.now());
        PassengerEntity passenger = createPassenger("123");
        PassengerReservationEntity savedPassengerReservation = new PassengerReservationEntity(passenger, savedReservation, null, SeatClass.ECONOMY);

        given(flightQueryService.getFlightEntity(5L)).willReturn(flight);
        given(reservationNumberGenerator.generate()).willReturn("ABC123");
        given(reservationRepository.save(any(ReservationEntity.class))).willReturn(savedReservation);
        given(passengerReservationRepository.countByFlightAndSeatClassAndStatusNot(flight, SeatClass.ECONOMY, PassengerReservationStatus.CANCELED)).willReturn(0L);
        given(passengerService.createOrGetPassenger(addPassengerDto)).willReturn(passenger);
        given(passengerReservationRepository.saveAll(any())).willReturn(List.of(savedPassengerReservation));
        given(flightMapper.toDto(any(FlightEntity.class))).willReturn(new com.falcon.booking.feature.flight.dto.ResponseFlightDto(1L, "AV1234", "BOG", "MDE", null, null, 100, null, com.falcon.booking.common.enums.FlightStatus.SCHEDULED));
        given(passengerReservationMapper.toResponseDto(List.of(savedPassengerReservation)))
                .willReturn(List.of(new ResponsePassengerReservationDto(null, null, SeatClass.ECONOMY, PassengerReservationStatus.RESERVED)));

        ResponseReservationDto result = reservationCommandService.addReservation(request);

        assertThat(result.number()).isEqualTo("ABC123");
        verify(passengerReservationRepository).saveAll(any());
    }

    @DisplayName("Should throw exception when flight can not be reserved")
    @Test
    void shouldThrowException_addReservation() {
        FlightEntity flight = createFlight(FlightStatus.CANCELED, 100, 10);
        AddReservationDto request = new AddReservationDto(5L, "contact@test.com", List.of());
        given(flightQueryService.getFlightEntity(5L)).willReturn(flight);

        assertThrows(FlightCanNotBeReservedException.class, () -> reservationCommandService.addReservation(request));
        verify(reservationRepository, never()).save(any());
    }

    @DisplayName("Should throw exception when reservation has no passengers")
    @Test
    void shouldThrowExceptionNoPassengers_addReservation() {
        FlightEntity flight = createFlight(FlightStatus.SCHEDULED, 100, 10);
        AddReservationDto request = new AddReservationDto(5L, "contact@test.com", List.of());

        given(flightQueryService.getFlightEntity(5L)).willReturn(flight);
        given(reservationNumberGenerator.generate()).willReturn("ABC123");
        given(reservationRepository.save(any(ReservationEntity.class)))
                .willReturn(new ReservationEntity("ABC123", flight, "contact@test.com", Instant.now()));

        assertThrows(ReservationMustHavePassengersException.class, () -> reservationCommandService.addReservation(request));
    }

    @DisplayName("Should throw exception when flight economy capacity is exceeded")
    @Test
    void shouldThrowExceptionFlightCapacityExceeded_addReservation() {
        FlightEntity flight = createFlight(FlightStatus.SCHEDULED, 10, 5); // Economy capacity 10
        AddPassengerDto passenger1 = new AddPassengerDto("Ana", "Perez", PassengerGender.F,
                "CO", LocalDate.now().minusYears(20), "P123", "123");

        AddReservationDto request = new AddReservationDto(5L, "contact@test.com", List.of(
                new AddPassengerReservationDto(passenger1, null, SeatClass.ECONOMY)
        ));

        given(flightQueryService.getFlightEntity(5L)).willReturn(flight);
        given(reservationNumberGenerator.generate()).willReturn("ABC123");
        given(reservationRepository.save(any(ReservationEntity.class)))
                .willReturn(new ReservationEntity("ABC123", flight, "contact@test.com", Instant.now()));
        given(passengerReservationRepository.countByFlightAndSeatClassAndStatusNot(flight, SeatClass.ECONOMY, PassengerReservationStatus.CANCELED)).willReturn(10L); // Economy is full

        assertThrows(FlightCapacityExceededException.class, () -> reservationCommandService.addReservation(request));
    }

    @DisplayName("Should throw exception when flight first class capacity is exceeded")
    @Test
    void shouldThrowExceptionFirstClassCapacityExceeded_addReservation() {
        FlightEntity flight = createFlight(FlightStatus.SCHEDULED, 100, 5); // First class capacity 5
        AddPassengerDto passenger1 = new AddPassengerDto("Ana", "Perez", PassengerGender.F,
                "CO", LocalDate.now().minusYears(20), "P123", "123");

        AddReservationDto request = new AddReservationDto(5L, "contact@test.com", List.of(
                new AddPassengerReservationDto(passenger1, null, SeatClass.FIRST_CLASS)
        ));

        given(flightQueryService.getFlightEntity(5L)).willReturn(flight);
        given(reservationNumberGenerator.generate()).willReturn("ABC123");
        given(reservationRepository.save(any(ReservationEntity.class)))
                .willReturn(new ReservationEntity("ABC123", flight, "contact@test.com", Instant.now()));
        given(passengerReservationRepository.countByFlightAndSeatClassAndStatusNot(flight, SeatClass.FIRST_CLASS, PassengerReservationStatus.CANCELED)).willReturn(5L); // First class is full

        assertThrows(FlightCapacityExceededException.class, () -> reservationCommandService.addReservation(request));
    }
}
