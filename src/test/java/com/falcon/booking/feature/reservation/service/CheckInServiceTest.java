package com.falcon.booking.feature.reservation.service;

import com.falcon.booking.common.enums.FlightStatus;
import com.falcon.booking.common.enums.PassengerGender;
import com.falcon.booking.common.enums.PassengerReservationStatus;
import com.falcon.booking.common.enums.SeatClass;
import com.falcon.booking.feature.checkIn.exception.InvalidCheckInPassengerReservationStatusException;
import com.falcon.booking.feature.checkIn.exception.SeatNumberOutOfRangeException;
import com.falcon.booking.feature.checkIn.service.CheckInService;
import com.falcon.booking.feature.flight.service.FlightQueryService;
import com.falcon.booking.feature.passenger.service.PassengerService;
import com.falcon.booking.feature.reservation.dto.ResponsePassengerReservationDto;
import com.falcon.booking.feature.reservation.mapper.PassengerReservationMapper;
import com.falcon.booking.persistence.entity.*;
import com.falcon.booking.persistence.repository.PassengerReservationRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

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
public class CheckInServiceTest {

    @Mock
    private PassengerService passengerService;
    @Mock
    private PassengerReservationMapper passengerReservationMapper;
    @Mock
    private ReservationQueryService reservationQueryService;
    @Mock
    private ReservationAccessService reservationAccessService;
    @Mock
    private PassengerReservationRepository passengerReservationRepository;
    @Mock
    private FlightQueryService flightQueryService;

    @InjectMocks
    private CheckInService checkInService;

    private CountryEntity createCountry(String isoCode) {
        CountryEntity country = new CountryEntity();
        country.setIsoCode(isoCode);
        country.setName("Country " + isoCode);
        return country;
    }

    private PassengerEntity createPassenger(String identificationNumber) {
        PassengerEntity passenger = new PassengerEntity("Ana", "Perez", PassengerGender.F,
                LocalDate.now().minusYears(20), "P" + identificationNumber, identificationNumber);
        passenger.setId(1L);
        passenger.setCountryNationality(createCountry("CO"));
        return passenger;
    }

    private FlightEntity createFlight(FlightStatus flightStatus) {
        AirplaneTypeEntity airplaneType = new AirplaneTypeEntity();
        airplaneType.configureSeats(100, 20, "ABCDEF"); // total 120: seats 1-20 first class, 21-120 economy

        FlightEntity flight = new FlightEntity();
        flight.setId(5L);
        flight.setDepartureDateTime(OffsetDateTime.now().plusHours(2));
        flight.setStatus(flightStatus);
        flight.setAirplaneType(airplaneType);
        return flight;
    }

    private ReservationEntity createReservationWithPassenger(PassengerEntity passenger, FlightEntity flight, SeatClass seatClass) {
        ReservationEntity reservation = new ReservationEntity("ABC123", flight, "contact@test.com", Instant.now());
        PassengerReservationEntity passengerReservation = new PassengerReservationEntity(passenger, reservation, null, seatClass);
        reservation.getPassengerReservations().add(passengerReservation);
        return reservation;
    }

    @DisplayName("Should check in passenger by identification number providing an economy seat")
    @Test
    void shouldCheckInByIdentificationNumberWithEconomySeat() {
        PassengerEntity passenger = createPassenger("123");
        FlightEntity flight = createFlight(FlightStatus.CHECK_IN_AVAILABLE);
        ReservationEntity reservation = createReservationWithPassenger(passenger, flight, SeatClass.ECONOMY);
        // Seat 25 is valid for economy (range 21-120)
        ResponsePassengerReservationDto response = new ResponsePassengerReservationDto(null, null, 25, SeatClass.ECONOMY, PassengerReservationStatus.CHECKED_IN);

        given(passengerService.getPassengerEntityByIdentificationNumber("123", "CO")).willReturn(passenger);
        given(reservationQueryService.getReservationEntityByNumber("ABC123")).willReturn(reservation);
        given(passengerReservationRepository.findAllByFlight(flight)).willReturn(List.of());
        given(passengerReservationMapper.toResponseDto(any(PassengerReservationEntity.class))).willReturn(response);

        ResponsePassengerReservationDto result = checkInService.checkInByIdentificationNumber("ABC123", "contact@test.com", "123", "CO", 25);

        assertThat(result).isEqualTo(response);
        assertThat(reservation.getPassengerReservations().get(0).getStatus()).isEqualTo(PassengerReservationStatus.CHECKED_IN);
        assertThat(reservation.getPassengerReservations().get(0).getSeatNumber()).isEqualTo(25);
        verify(passengerService).getPassengerEntityByIdentificationNumber("123", "CO");
        verify(reservationAccessService).getReservationByNumberAndContactEmail("ABC123", "contact@test.com");
        verify(reservationQueryService).getReservationEntityByNumber("ABC123");
        verify(passengerReservationMapper).toResponseDto(reservation.getPassengerReservations().get(0));
    }

    @DisplayName("Should check in first class passenger providing a first class seat")
    @Test
    void shouldCheckInByIdentificationNumberWithFirstClassSeat() {
        PassengerEntity passenger = createPassenger("123");
        FlightEntity flight = createFlight(FlightStatus.CHECK_IN_AVAILABLE);
        ReservationEntity reservation = createReservationWithPassenger(passenger, flight, SeatClass.FIRST_CLASS);
        // Seat 5 is valid for first class (range 1-20)
        ResponsePassengerReservationDto response = new ResponsePassengerReservationDto(null, null, 5, SeatClass.FIRST_CLASS, PassengerReservationStatus.CHECKED_IN);

        given(passengerService.getPassengerEntityByIdentificationNumber("123", "CO")).willReturn(passenger);
        given(reservationQueryService.getReservationEntityByNumber("ABC123")).willReturn(reservation);
        given(passengerReservationRepository.findAllByFlight(flight)).willReturn(List.of());
        given(passengerReservationMapper.toResponseDto(any(PassengerReservationEntity.class))).willReturn(response);

        ResponsePassengerReservationDto result = checkInService.checkInByIdentificationNumber("ABC123", "contact@test.com", "123", "CO", 5);

        assertThat(result).isEqualTo(response);
        assertThat(reservation.getPassengerReservations().get(0).getSeatNumber()).isEqualTo(5);
    }

    @DisplayName("Should throw exception when economy passenger requests a first class seat")
    @Test
    void shouldThrowExceptionWhenEconomyPassengerRequestsFirstClassSeat() {
        PassengerEntity passenger = createPassenger("123");
        FlightEntity flight = createFlight(FlightStatus.CHECK_IN_AVAILABLE);
        ReservationEntity reservation = createReservationWithPassenger(passenger, flight, SeatClass.ECONOMY);

        given(passengerService.getPassengerEntityByIdentificationNumber("123", "CO")).willReturn(passenger);
        given(reservationQueryService.getReservationEntityByNumber("ABC123")).willReturn(reservation);
        given(passengerReservationRepository.findAllByFlight(flight)).willReturn(List.of());

        // Seat 5 is a first class seat (range 1-20), not valid for economy (range 21-120)
        assertThrows(SeatNumberOutOfRangeException.class,
                () -> checkInService.checkInByIdentificationNumber("ABC123", "contact@test.com", "123", "CO", 5));
    }

    @DisplayName("Should check in passenger without providing seat, assigning random economy one")
    @Test
    void shouldCheckInWithoutSeat() {
        PassengerEntity passenger = createPassenger("123");
        FlightEntity flight = createFlight(FlightStatus.CHECK_IN_AVAILABLE);
        ReservationEntity reservation = createReservationWithPassenger(passenger, flight, SeatClass.ECONOMY);

        given(reservationQueryService.getReservationEntityByNumber("ABC123")).willReturn(reservation);
        given(passengerReservationRepository.findAllByFlight(flight)).willReturn(List.of());

        PassengerReservationEntity result = checkInService.checkIn("ABC123", passenger, null);

        assertThat(result.getPassenger()).isEqualTo(passenger);
        assertThat(result.getStatus()).isEqualTo(PassengerReservationStatus.CHECKED_IN);
        assertThat(result.getSeatNumber()).isNotNull();
        // Economy range: 21-120
        assertThat(result.getSeatNumber()).isGreaterThanOrEqualTo(21);
        assertThat(result.getSeatNumber()).isLessThanOrEqualTo(120);
        verify(reservationQueryService).getReservationEntityByNumber("ABC123");
    }

    @DisplayName("Should check in first class passenger without seat, assigning random first class seat")
    @Test
    void shouldCheckInFirstClassWithoutSeat() {
        PassengerEntity passenger = createPassenger("123");
        FlightEntity flight = createFlight(FlightStatus.CHECK_IN_AVAILABLE);
        ReservationEntity reservation = createReservationWithPassenger(passenger, flight, SeatClass.FIRST_CLASS);

        given(reservationQueryService.getReservationEntityByNumber("ABC123")).willReturn(reservation);
        given(passengerReservationRepository.findAllByFlight(flight)).willReturn(List.of());

        PassengerReservationEntity result = checkInService.checkIn("ABC123", passenger, null);

        assertThat(result.getSeatNumber()).isNotNull();
        // First class range: 1-20
        assertThat(result.getSeatNumber()).isGreaterThanOrEqualTo(1);
        assertThat(result.getSeatNumber()).isLessThanOrEqualTo(20);
    }

    @DisplayName("Should throw exception when passenger reservation is not reserved")
    @Test
    void shouldThrowExceptionWhenPassengerReservationIsNotReserved() {
        PassengerEntity passenger = createPassenger("123");
        FlightEntity flight = createFlight(FlightStatus.CHECK_IN_AVAILABLE);
        ReservationEntity reservation = createReservationWithPassenger(passenger, flight, SeatClass.ECONOMY);
        ReflectionTestUtils.setField(reservation.getPassengerReservations().get(0), "seatNumber", 25);
        reservation.checkInPassenger(passenger, 25); // Now it's checked in

        given(passengerService.getPassengerEntityByIdentificationNumber("123", "CO")).willReturn(passenger);
        given(reservationQueryService.getReservationEntityByNumber("ABC123")).willReturn(reservation);

        assertThrows(InvalidCheckInPassengerReservationStatusException.class,
                () -> checkInService.checkInByIdentificationNumber("ABC123", "contact@test.com", "123", "CO", 25));

        verify(passengerReservationMapper, never()).toResponseDto(any(PassengerReservationEntity.class));
    }

    @DisplayName("Should return all passenger reservations for a given flight")
    @Test
    void shouldReturnPassengerReservationsByFlight() {
        FlightEntity flight = createFlight(FlightStatus.CHECK_IN_AVAILABLE);
        PassengerEntity passenger = createPassenger("123");
        ReservationEntity reservation = createReservationWithPassenger(passenger, flight, SeatClass.ECONOMY);
        PassengerReservationEntity pr = reservation.getPassengerReservations().get(0);

        given(passengerReservationRepository.findAllByFlight(flight)).willReturn(List.of(pr));

        List<PassengerReservationEntity> result = checkInService.getPassengerReservationsByFlight(flight);

        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isSameAs(pr);
        verify(passengerReservationRepository).findAllByFlight(flight);
    }
}
