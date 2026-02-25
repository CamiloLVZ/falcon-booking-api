package com.falcon.booking.persistence.entity;

import com.falcon.booking.domain.exception.Flight.OutOfFlightBoardingTimeException;
import com.falcon.booking.domain.exception.Flight.OutOfFlightCheckInTimeException;
import com.falcon.booking.domain.exception.Reservation.InvalidBoardingPassengerReservationException;
import com.falcon.booking.domain.exception.Reservation.InvalidCheckInPassengerReservationException;
import com.falcon.booking.domain.exception.Reservation.ReservationInvalidStatusChangeException;
import com.falcon.booking.domain.valueobject.FlightStatus;
import com.falcon.booking.domain.valueobject.PassengerGender;
import com.falcon.booking.domain.valueobject.PassengerReservationStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class PassengerReservationEntityTest {

    private CountryEntity createCountry(String isoCode) {
        CountryEntity countryEntity = new CountryEntity();
        countryEntity.setIsoCode(isoCode);
        countryEntity.setName("Country " + isoCode);
        return countryEntity;
    }

    private PassengerEntity createPassenger(String identificationNumber, CountryEntity country) {
        PassengerEntity passengerEntity = new PassengerEntity();
        passengerEntity.setFirstName("Juan");
        passengerEntity.setLastName("Perez");
        passengerEntity.setGender(PassengerGender.M);
        passengerEntity.setDateOfBirth(LocalDate.of(1990, 1, 10));
        passengerEntity.setIdentificationNumber(identificationNumber);
        passengerEntity.setPassportNumber("AB1234");
        passengerEntity.setCountryNationality(country);
        return passengerEntity;
    }

    private FlightEntity createFlight(Long id, FlightStatus status) {
        FlightEntity flightEntity = new FlightEntity();
        flightEntity.setId(id);
        flightEntity.setStatus(status);
        flightEntity.setDepartureDateTime(OffsetDateTime.now().plusHours(2));
        return flightEntity;
    }

    private ReservationEntity createReservation(String number, FlightEntity flightEntity) {
        return new ReservationEntity(number, flightEntity, "test@email.com", Instant.now());
    }

    private void setStatus(PassengerReservationEntity passengerReservationEntity, PassengerReservationStatus status) {
        try {
            Field field = PassengerReservationEntity.class.getDeclaredField("status");
            field.setAccessible(true);
            field.set(passengerReservationEntity, status);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @DisplayName("Should set flight, seat and reserved status when created")
    @Test
    void shouldInitializeFields_constructor() {
        FlightEntity flightEntity = createFlight(1L, FlightStatus.SCHEDULED);
        ReservationEntity reservationEntity = createReservation("rsv001", flightEntity);
        PassengerEntity passengerEntity = createPassenger("10001", createCountry("CO"));

        PassengerReservationEntity passengerReservationEntity =
                new PassengerReservationEntity(passengerEntity, reservationEntity, 12);

        assertThat(passengerReservationEntity.getPassenger()).isEqualTo(passengerEntity);
        assertThat(passengerReservationEntity.getReservation()).isEqualTo(reservationEntity);
        assertThat(passengerReservationEntity.getFlight()).isEqualTo(flightEntity);
        assertThat(passengerReservationEntity.getSeatNumber()).isEqualTo(12);
        assertThat(passengerReservationEntity.getStatus()).isEqualTo(PassengerReservationStatus.RESERVED);
    }

    @DisplayName("Should cancel passenger reservation from reserved status")
    @Test
    void shouldCancel_cancel() {
        FlightEntity flightEntity = createFlight(1L, FlightStatus.SCHEDULED);
        ReservationEntity reservationEntity = createReservation("rsv001", flightEntity);
        PassengerReservationEntity passengerReservationEntity =
                new PassengerReservationEntity(createPassenger("10001", createCountry("CO")), reservationEntity, 12);

        passengerReservationEntity.cancel();

        assertThat(passengerReservationEntity.isCanceled()).isTrue();
    }

    @DisplayName("Should keep canceled status when cancel is called again")
    @Test
    void shouldKeepCanceled_cancel() {
        FlightEntity flightEntity = createFlight(1L, FlightStatus.SCHEDULED);
        ReservationEntity reservationEntity = createReservation("rsv001", flightEntity);
        PassengerReservationEntity passengerReservationEntity =
                new PassengerReservationEntity(createPassenger("10001", createCountry("CO")), reservationEntity, 12);
        setStatus(passengerReservationEntity, PassengerReservationStatus.CANCELED);

        passengerReservationEntity.cancel();

        assertThat(passengerReservationEntity.isCanceled()).isTrue();
    }

    @DisplayName("Should throw exception when canceling from boarded status")
    @Test
    void shouldThrowException_cancel() {
        FlightEntity flightEntity = createFlight(1L, FlightStatus.SCHEDULED);
        ReservationEntity reservationEntity = createReservation("rsv001", flightEntity);
        PassengerReservationEntity passengerReservationEntity =
                new PassengerReservationEntity(createPassenger("10001", createCountry("CO")), reservationEntity, 12);
        setStatus(passengerReservationEntity, PassengerReservationStatus.BOARDED);

        ReservationInvalidStatusChangeException exception =
                assertThrows(ReservationInvalidStatusChangeException.class, passengerReservationEntity::cancel);

        assertThat(exception.getMessage()).contains("BOARDED");
    }

    @DisplayName("Should check in passenger reservation when flight allows check in")
    @Test
    void shouldCheckIn_checkIn() {
        FlightEntity flightEntity = createFlight(1L, FlightStatus.CHECK_IN_AVAILABLE);
        ReservationEntity reservationEntity = createReservation("rsv001", flightEntity);
        PassengerReservationEntity passengerReservationEntity =
                new PassengerReservationEntity(createPassenger("10001", createCountry("CO")), reservationEntity, 12);

        passengerReservationEntity.checkIn();

        assertThat(passengerReservationEntity.isCheckedIn()).isTrue();
    }

    @DisplayName("Should throw exception when check in is attempted from canceled status")
    @Test
    void shouldThrowException_checkIn() {
        FlightEntity flightEntity = createFlight(1L, FlightStatus.CHECK_IN_AVAILABLE);
        ReservationEntity reservationEntity = createReservation("rsv001", flightEntity);
        PassengerReservationEntity passengerReservationEntity =
                new PassengerReservationEntity(createPassenger("10001", createCountry("CO")), reservationEntity, 12);
        setStatus(passengerReservationEntity, PassengerReservationStatus.CANCELED);

        assertThrows(InvalidCheckInPassengerReservationException.class, passengerReservationEntity::checkIn);
    }

    @DisplayName("Should throw exception when check in is out of flight check in time")
    @Test
    void shouldThrowOutOfCheckInTime_checkIn() {
        FlightEntity flightEntity = createFlight(55L, FlightStatus.SCHEDULED);
        ReservationEntity reservationEntity = createReservation("rsv001", flightEntity);
        PassengerReservationEntity passengerReservationEntity =
                new PassengerReservationEntity(createPassenger("10001", createCountry("CO")), reservationEntity, 12);

        OutOfFlightCheckInTimeException exception =
                assertThrows(OutOfFlightCheckInTimeException.class, passengerReservationEntity::checkIn);

        assertThat(exception.getMessage()).contains("55");
    }

    @DisplayName("Should board passenger reservation when in boarding and checked in")
    @Test
    void shouldBoard_board() {
        FlightEntity flightEntity = createFlight(1L, FlightStatus.BOARDING);
        ReservationEntity reservationEntity = createReservation("rsv001", flightEntity);
        PassengerReservationEntity passengerReservationEntity =
                new PassengerReservationEntity(createPassenger("10001", createCountry("CO")), reservationEntity, 12);
        setStatus(passengerReservationEntity, PassengerReservationStatus.CHECKED_IN);

        passengerReservationEntity.board();

        assertThat(passengerReservationEntity.isBoarded()).isTrue();
    }

    @DisplayName("Should throw exception when board is attempted without check in")
    @Test
    void shouldThrowException_board() {
        FlightEntity flightEntity = createFlight(1L, FlightStatus.BOARDING);
        ReservationEntity reservationEntity = createReservation("rsv001", flightEntity);
        PassengerReservationEntity passengerReservationEntity =
                new PassengerReservationEntity(createPassenger("10001", createCountry("CO")), reservationEntity, 12);

        assertThrows(InvalidBoardingPassengerReservationException.class, passengerReservationEntity::board);
    }

    @DisplayName("Should throw exception when board is out of boarding time")
    @Test
    void shouldThrowOutOfBoardingTime_board() {
        FlightEntity flightEntity = createFlight(77L, FlightStatus.CHECK_IN_AVAILABLE);
        ReservationEntity reservationEntity = createReservation("rsv001", flightEntity);
        PassengerReservationEntity passengerReservationEntity =
                new PassengerReservationEntity(createPassenger("10001", createCountry("CO")), reservationEntity, 12);
        setStatus(passengerReservationEntity, PassengerReservationStatus.CHECKED_IN);

        OutOfFlightBoardingTimeException exception =
                assertThrows(OutOfFlightBoardingTimeException.class, passengerReservationEntity::board);

        assertThat(exception.getMessage()).contains("77");
    }

    @DisplayName("Should return state helpers correctly for each status")
    @Test
    void shouldReturnStateHelpers_statusChecks() {
        FlightEntity flightEntity = createFlight(1L, FlightStatus.SCHEDULED);
        ReservationEntity reservationEntity = createReservation("rsv001", flightEntity);
        PassengerReservationEntity passengerReservationEntity =
                new PassengerReservationEntity(createPassenger("10001", createCountry("CO")), reservationEntity, 12);

        assertThat(passengerReservationEntity.isReserved()).isTrue();
        assertThat(passengerReservationEntity.isCheckedIn()).isFalse();
        assertThat(passengerReservationEntity.isCanceled()).isFalse();
        assertThat(passengerReservationEntity.isBoarded()).isFalse();

        setStatus(passengerReservationEntity, PassengerReservationStatus.CHECKED_IN);
        assertThat(passengerReservationEntity.isCheckedIn()).isTrue();

        setStatus(passengerReservationEntity, PassengerReservationStatus.CANCELED);
        assertThat(passengerReservationEntity.isCanceled()).isTrue();

        setStatus(passengerReservationEntity, PassengerReservationStatus.BOARDED);
        assertThat(passengerReservationEntity.isBoarded()).isTrue();
    }

    @DisplayName("Equals methods for entities with same passenger and reservation should return true")
    @Test
    void shouldReturnTrue_samePassengerReservationInEquals() {
        FlightEntity flightEntity = createFlight(1L, FlightStatus.SCHEDULED);
        ReservationEntity reservationEntity = createReservation("rsv001", flightEntity);
        PassengerEntity passengerEntity = createPassenger("10001", createCountry("CO"));

        PassengerReservationEntity first = new PassengerReservationEntity(passengerEntity, reservationEntity, 1);
        PassengerReservationEntity second = new PassengerReservationEntity(passengerEntity, reservationEntity, 7);

        assertThat(first.equals(second)).isTrue();
    }

    @DisplayName("Hash code for entities with different passenger should not be equal")
    @Test
    void shouldNotBeEqual_differentPassengerReservationHashCode() {
        FlightEntity flightEntity = createFlight(1L, FlightStatus.SCHEDULED);
        ReservationEntity reservationEntity = createReservation("rsv001", flightEntity);

        PassengerReservationEntity first =
                new PassengerReservationEntity(createPassenger("10001", createCountry("CO")), reservationEntity, 1);
        PassengerReservationEntity second =
                new PassengerReservationEntity(createPassenger("10002", createCountry("CO")), reservationEntity, 1);

        assertThat(first.hashCode()).isNotEqualTo(second.hashCode());
    }
}
