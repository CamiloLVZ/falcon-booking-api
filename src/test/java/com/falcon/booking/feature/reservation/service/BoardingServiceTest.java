package com.falcon.booking.feature.reservation.service;

import com.falcon.booking.common.enums.FlightStatus;
import com.falcon.booking.common.enums.PassengerGender;
import com.falcon.booking.common.enums.PassengerReservationStatus;
import com.falcon.booking.feature.passenger.service.PassengerService;
import com.falcon.booking.feature.reservation.dto.ResponsePassengerReservationDto;
import com.falcon.booking.feature.reservation.exception.InvalidBoardingPassengerReservationException;
import com.falcon.booking.feature.reservation.mapper.PassengerReservationMapper;
import com.falcon.booking.persistence.entity.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class BoardingServiceTest {

    @Mock
    private PassengerService passengerService;
    @Mock
    private PassengerReservationMapper passengerReservationMapper;
    @Mock
    private ReservationQueryService reservationQueryService;

    @InjectMocks
    private BoardingService boardingService;

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

    private ReservationEntity createReservationWithPassenger(PassengerEntity passenger, FlightStatus flightStatus) {
        FlightEntity flight = new FlightEntity();
        flight.setId(5L);
        flight.setDepartureDateTime(OffsetDateTime.now().plusHours(2));
        flight.setStatus(flightStatus);

        ReservationEntity reservation = new ReservationEntity("ABC123", flight, "contact@test.com", Instant.now());
        PassengerReservationEntity passengerReservation = new PassengerReservationEntity(passenger, reservation, 8);
        reservation.getPassengerReservations().add(passengerReservation);
        return reservation;
    }

    private ReservationEntity createReservationWithCheckedInPassenger(PassengerEntity passenger) {
        ReservationEntity reservation = createReservationWithPassenger(passenger, FlightStatus.CHECK_IN_AVAILABLE);
        reservation.checkInPassenger(passenger);
        reservation.getFlight().setStatus(FlightStatus.BOARDING);
        return reservation;
    }

    @DisplayName("Should board passenger by identification number")
    @Test
    void shouldBoardByIdentificationNumber() {
        PassengerEntity passenger = createPassenger("123");
        ReservationEntity reservation = createReservationWithCheckedInPassenger(passenger);
        ResponsePassengerReservationDto response = new ResponsePassengerReservationDto(null, 8, PassengerReservationStatus.BOARDED);

        given(passengerService.getPassengerEntityByIdentificationNumber("123", "CO")).willReturn(passenger);
        given(reservationQueryService.getReservationEntityByNumber("ABC123")).willReturn(reservation);
        given(passengerReservationMapper.toResponseDto(any(PassengerReservationEntity.class))).willReturn(response);

        ResponsePassengerReservationDto result = boardingService.boardByIdentificationNumber("ABC123", "123", "CO");

        assertThat(result).isEqualTo(response);
        assertThat(reservation.getPassengerReservations().get(0).getStatus()).isEqualTo(PassengerReservationStatus.BOARDED);
        verify(passengerService).getPassengerEntityByIdentificationNumber("123", "CO");
        verify(reservationQueryService).getReservationEntityByNumber("ABC123");
        verify(passengerReservationMapper).toResponseDto(reservation.getPassengerReservations().get(0));
    }

    @DisplayName("Should board passenger")
    @Test
    void shouldBoard() {
        PassengerEntity passenger = createPassenger("123");
        ReservationEntity reservation = createReservationWithCheckedInPassenger(passenger);
        given(reservationQueryService.getReservationEntityByNumber("ABC123")).willReturn(reservation);

        PassengerReservationEntity result = boardingService.board("ABC123", passenger);

        assertThat(result.getPassenger()).isEqualTo(passenger);
        assertThat(result.getStatus()).isEqualTo(PassengerReservationStatus.BOARDED);
        verify(reservationQueryService).getReservationEntityByNumber("ABC123");
    }

    @DisplayName("Should throw exception when passenger reservation is not checked in")
    @Test
    void shouldThrowExceptionWhenPassengerReservationIsNotCheckedIn() {
        PassengerEntity passenger = createPassenger("123");
        ReservationEntity reservation = createReservationWithPassenger(passenger, FlightStatus.BOARDING);

        given(passengerService.getPassengerEntityByIdentificationNumber("123", "CO")).willReturn(passenger);
        given(reservationQueryService.getReservationEntityByNumber("ABC123")).willReturn(reservation);

        assertThrows(InvalidBoardingPassengerReservationException.class,
                () -> boardingService.boardByIdentificationNumber("ABC123", "123", "CO"));

        verify(passengerReservationMapper, never()).toResponseDto(any(PassengerReservationEntity.class));
    }
}
