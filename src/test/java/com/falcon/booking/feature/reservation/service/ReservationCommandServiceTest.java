package com.falcon.booking.feature.reservation.service;

import com.falcon.booking.common.enums.AirplaneTypeStatus;
import com.falcon.booking.common.enums.FlightStatus;
import com.falcon.booking.common.enums.PassengerGender;
import com.falcon.booking.common.enums.RouteStatus;
import com.falcon.booking.feature.flight.service.FlightQueryService;
import com.falcon.booking.feature.passenger.service.PassengerService;
import com.falcon.booking.feature.reservation.mapper.PassengerReservationMapper;
import com.falcon.booking.persistence.entity.*;
import com.falcon.booking.persistence.repository.PassengerReservationRepository;
import com.falcon.booking.persistence.repository.ReservationRepository;
import com.falcon.booking.feature.reservation.mapper.ReservationMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.falcon.booking.common.enums.PassengerReservationStatus;
import com.falcon.booking.common.enums.SeatClass;
import com.falcon.booking.feature.passenger.dto.AddPassengerDto;
import com.falcon.booking.feature.passenger.exception.PassengerNotFoundException;
import com.falcon.booking.feature.payment.dto.PaymentPassengerDto;
import com.falcon.booking.feature.payment.dto.PaymentRequestDto;
import com.falcon.booking.feature.reservation.component.ReservationNumberGenerator;
import com.falcon.booking.feature.reservation.exception.PassengerAlreadyReservedFlightException;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

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
    private ReservationMapper reservationMapper;
    @Mock
    private ReservationQueryService reservationQueryService;
    @Mock
    private ReservationNumberGenerator reservationNumberGenerator;

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
        airplaneType.configureSeats(economySeats, firstClassSeats, "ABCDEF");
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


    @DisplayName("Should create reservation from payment successfully")
    @Test
    void shouldCreateReservationFromPayment() {
        FlightEntity flight = createFlight(FlightStatus.SCHEDULED, 108, 12);

        AddPassengerDto addPassengerDto =
                new AddPassengerDto(
                        "Ana", "Perez", PassengerGender.F, "CO", LocalDate.now().minusYears(20), "P123456", "123456");
        PaymentPassengerDto paymentPassengerDto =
                new PaymentPassengerDto(
                        addPassengerDto, SeatClass.ECONOMY);

        PaymentRequestDto requestDto =
                new PaymentRequestDto(
                        flight.getId(), "test@test.com", List.of(paymentPassengerDto));

        PassengerEntity passenger = createPassenger("123456");

        when(passengerService.getPassengerEntityByIdentificationNumber("123456", "CO"))
                .thenThrow(new PassengerNotFoundException("123456"));
        when(reservationNumberGenerator.generate()).thenReturn("RES123");
        when(reservationRepository.save(any(ReservationEntity.class))).thenAnswer(i -> i.getArguments()[0]);
        when(passengerService.createOrGetPassenger(addPassengerDto)).thenReturn(passenger);

        String reservationNumber = reservationCommandService.createReservationFromPayment(requestDto, flight);

        assertThat(reservationNumber).isEqualTo("RES123");
        verify(passengerReservationRepository).saveAll(anyList());
    }

    @DisplayName("Should fail to create reservation if passenger already reserved")
    @Test
    void shouldFailCreateReservation_PassengerAlreadyReserved() {
        FlightEntity flight = createFlight(FlightStatus.SCHEDULED, 108, 12);

        AddPassengerDto addPassengerDto =
                new AddPassengerDto(
                        "Ana", "Perez", PassengerGender.F, "CO", LocalDate.now().minusYears(20), "P123456", "123456");
        PaymentPassengerDto paymentPassengerDto =
                new PaymentPassengerDto(
                        addPassengerDto, SeatClass.ECONOMY);

        PaymentRequestDto requestDto =
                new PaymentRequestDto(
                        flight.getId(), "test@test.com", List.of(paymentPassengerDto));

        PassengerEntity passenger = createPassenger("123456");

        when(passengerService.getPassengerEntityByIdentificationNumber("123456", "CO"))
                .thenReturn(passenger);
        when(passengerReservationRepository.findAllByFlightAndPassengerAndStatusNot(
                eq(flight),
                eq(passenger),
                eq(PassengerReservationStatus.CANCELED)))
                .thenReturn(List.of(new PassengerReservationEntity()));

        assertThatThrownBy(() -> reservationCommandService.createReservationFromPayment(requestDto, flight))
                .isInstanceOf(PassengerAlreadyReservedFlightException.class);
    }
}
