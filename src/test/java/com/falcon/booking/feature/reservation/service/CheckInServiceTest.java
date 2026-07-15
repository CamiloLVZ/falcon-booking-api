package com.falcon.booking.feature.reservation.service;

import com.falcon.booking.common.enums.FlightStatus;
import com.falcon.booking.common.enums.PassengerGender;
import com.falcon.booking.common.enums.PassengerReservationStatus;
import com.falcon.booking.feature.passenger.service.PassengerService;
import com.falcon.booking.feature.reservation.dto.ResponsePassengerReservationDto;
import com.falcon.booking.feature.reservation.exception.InvalidCheckInPassengerReservationException;
import com.falcon.booking.feature.reservation.mapper.PassengerReservationMapper;
import com.falcon.booking.persistence.entity.*;
import com.falcon.booking.persistence.repository.PassengerReservationRepository;
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
public class CheckInServiceTest {

    @Mock
    private PassengerService passengerService;
    @Mock
    private PassengerReservationMapper passengerReservationMapper;
    @Mock
    private ReservationQueryService reservationQueryService;
    @Mock
    private PassengerReservationRepository passengerReservationRepository;

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
        airplaneType.setEconomySeats(100);
        airplaneType.setFirstClassSeats(20); // total 120

        FlightEntity flight = new FlightEntity();
        flight.setId(5L);
        flight.setDepartureDateTime(OffsetDateTime.now().plusHours(2));
        flight.setStatus(flightStatus);
        flight.setAirplaneType(airplaneType);
        return flight;
    }

    private ReservationEntity createReservationWithPassenger(PassengerEntity passenger, FlightEntity flight) {
        ReservationEntity reservation = new ReservationEntity("ABC123", flight, "contact@test.com", Instant.now());
        PassengerReservationEntity passengerReservation = new PassengerReservationEntity(passenger, reservation, null);
        reservation.getPassengerReservations().add(passengerReservation);
        return reservation;
    }

    @DisplayName("Should check in passenger by identification number providing a seat")
    @Test
    void shouldCheckInByIdentificationNumberWithSeat() {
        PassengerEntity passenger = createPassenger("123");
        FlightEntity flight = createFlight(FlightStatus.CHECK_IN_AVAILABLE);
        ReservationEntity reservation = createReservationWithPassenger(passenger, flight);
        ResponsePassengerReservationDto response = new ResponsePassengerReservationDto(null, 8, PassengerReservationStatus.CHECKED_IN);

        given(passengerService.getPassengerEntityByIdentificationNumber("123", "CO")).willReturn(passenger);
        given(reservationQueryService.getReservationEntityByNumber("ABC123")).willReturn(reservation);
        given(passengerReservationRepository.findAllByFlight(flight)).willReturn(List.of());
        given(passengerReservationMapper.toResponseDto(any(PassengerReservationEntity.class))).willReturn(response);

        ResponsePassengerReservationDto result = checkInService.checkInByIdentificationNumber("ABC123", "123", "CO", 8);

        assertThat(result).isEqualTo(response);
        assertThat(reservation.getPassengerReservations().get(0).getStatus()).isEqualTo(PassengerReservationStatus.CHECKED_IN);
        assertThat(reservation.getPassengerReservations().get(0).getSeatNumber()).isEqualTo(8);
        verify(passengerService).getPassengerEntityByIdentificationNumber("123", "CO");
        verify(reservationQueryService).getReservationEntityByNumber("ABC123");
        verify(passengerReservationMapper).toResponseDto(reservation.getPassengerReservations().get(0));
    }

    @DisplayName("Should check in passenger without providing seat, assigning random one")
    @Test
    void shouldCheckInWithoutSeat() {
        PassengerEntity passenger = createPassenger("123");
        FlightEntity flight = createFlight(FlightStatus.CHECK_IN_AVAILABLE);
        ReservationEntity reservation = createReservationWithPassenger(passenger, flight);

        given(reservationQueryService.getReservationEntityByNumber("ABC123")).willReturn(reservation);
        given(passengerReservationRepository.findAllByFlight(flight)).willReturn(List.of());

        PassengerReservationEntity result = checkInService.checkIn("ABC123", passenger, null);

        assertThat(result.getPassenger()).isEqualTo(passenger);
        assertThat(result.getStatus()).isEqualTo(PassengerReservationStatus.CHECKED_IN);
        assertThat(result.getSeatNumber()).isNotNull();
        assertThat(result.getSeatNumber()).isGreaterThan(0);
        verify(reservationQueryService).getReservationEntityByNumber("ABC123");
    }

    @DisplayName("Should throw exception when passenger reservation is not reserved")
    @Test
    void shouldThrowExceptionWhenPassengerReservationIsNotReserved() {
        PassengerEntity passenger = createPassenger("123");
        FlightEntity flight = createFlight(FlightStatus.CHECK_IN_AVAILABLE);
        ReservationEntity reservation = createReservationWithPassenger(passenger, flight);
        reservation.getPassengerReservations().get(0).setSeatNumber(8);
        reservation.checkInPassenger(passenger, 8); // Now it's checked in

        given(passengerService.getPassengerEntityByIdentificationNumber("123", "CO")).willReturn(passenger);
        given(reservationQueryService.getReservationEntityByNumber("ABC123")).willReturn(reservation);

        assertThrows(InvalidCheckInPassengerReservationException.class,
                () -> checkInService.checkInByIdentificationNumber("ABC123", "123", "CO", 8));

        verify(passengerReservationMapper, never()).toResponseDto(any(PassengerReservationEntity.class));
    }
}
