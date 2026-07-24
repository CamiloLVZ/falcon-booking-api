package com.falcon.booking.feature.payment.service;

import com.falcon.booking.common.enums.FlightStatus;
import com.falcon.booking.common.enums.PassengerGender;
import com.falcon.booking.common.enums.PassengerReservationStatus;
import com.falcon.booking.common.enums.PaymentStatus;
import com.falcon.booking.common.enums.SeatClass;
import com.falcon.booking.feature.flight.exception.FlightCanNotBeReservedException;
import com.falcon.booking.feature.flight.service.FlightQueryService;
import com.falcon.booking.feature.passenger.dto.AddPassengerDto;
import com.falcon.booking.feature.payment.dto.PaymentPassengerDto;
import com.falcon.booking.feature.payment.dto.PaymentRequestDto;
import com.falcon.booking.feature.payment.dto.ResponsePaymentDto;
import com.falcon.booking.feature.reservation.exception.DuplicatedPassengerException;
import com.falcon.booking.feature.reservation.exception.FlightCapacityExceededException;
import com.falcon.booking.feature.reservation.service.ReservationCommandService;
import com.falcon.booking.persistence.entity.*;
import com.falcon.booking.persistence.repository.PassengerReservationRepository;
import com.falcon.booking.persistence.repository.PaymentRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private FlightQueryService flightQueryService;
    @Mock
    private ReservationCommandService reservationCommandService;
    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private PassengerReservationRepository passengerReservationRepository;

    @InjectMocks
    private PaymentService paymentService;

    /**
     * AddPassengerDto record field order:
     * firstName, lastName, gender, nationalityIsoCode, dateOfBirth, passportNumber, identificationNumber
     */
    private AddPassengerDto createAddPassengerDto(String id) {
        return new AddPassengerDto("Name", "Last", PassengerGender.M, "CO", LocalDate.now().minusYears(25), "P" + id, id);
    }

    private FlightEntity createFlight(FlightStatus status, int economySeats, int firstClassSeats) {
        AirplaneTypeEntity airplaneType = new AirplaneTypeEntity();
        airplaneType.configureSeats(economySeats, firstClassSeats, "ABCDEF");

        RouteEntity route = new RouteEntity();
        route.setBasePriceEconomy(BigDecimal.valueOf(100));
        route.setBasePriceFirstClass(BigDecimal.valueOf(300));

        FlightEntity flight = new FlightEntity();
        flight.setId(1L);
        flight.setAirplaneType(airplaneType);
        flight.setRoute(route);
        flight.setStatus(status);
        flight.setDepartureDateTime(OffsetDateTime.now().plusDays(2));
        flight.setBasePriceEconomy(BigDecimal.valueOf(100));
        flight.setBasePriceFirstClass(BigDecimal.valueOf(300));
        return flight;
    }

    @DisplayName("Should process payment successfully and return APPROVED status")
    @Test
    void shouldProcessPayment() {
        FlightEntity flight = createFlight(FlightStatus.SCHEDULED, 100, 20);
        AddPassengerDto p1 = createAddPassengerDto("1001");
        AddPassengerDto p2 = createAddPassengerDto("1002");
        PaymentPassengerDto pp1 = new PaymentPassengerDto(p1, SeatClass.ECONOMY);
        PaymentPassengerDto pp2 = new PaymentPassengerDto(p2, SeatClass.FIRST_CLASS);
        PaymentRequestDto requestDto = new PaymentRequestDto(flight.getId(), "test@test.com", List.of(pp1, pp2));

        when(flightQueryService.getFlightEntity(flight.getId())).thenReturn(flight);
        when(passengerReservationRepository.countByFlightAndSeatClassAndStatusNot(flight, SeatClass.FIRST_CLASS, PassengerReservationStatus.CANCELED)).thenReturn(5);
        when(passengerReservationRepository.countByFlightAndSeatClassAndStatusNot(flight, SeatClass.ECONOMY, PassengerReservationStatus.CANCELED)).thenReturn(50);
        when(reservationCommandService.createReservationFromPayment(requestDto, flight)).thenReturn("RES123");
        when(paymentRepository.save(any(PaymentEntity.class))).thenAnswer(i -> i.getArguments()[0]);

        ResponsePaymentDto response = paymentService.processPayment(requestDto);

        assertThat(response).isNotNull();
        assertThat(response.reservationNumber()).isEqualTo("RES123");
        assertThat(response.status()).isEqualTo(PaymentStatus.APPROVED);
        assertThat(response.totalAmount()).isGreaterThan(BigDecimal.ZERO);
        verify(paymentRepository).save(any(PaymentEntity.class));
    }

    @DisplayName("Should fail when flight cannot be reserved (e.g., CANCELED)")
    @Test
    void shouldFail_FlightCannotBeReserved() {
        FlightEntity flight = createFlight(FlightStatus.CANCELED, 100, 20);
        PaymentRequestDto requestDto = new PaymentRequestDto(flight.getId(), "test@test.com", List.of());

        when(flightQueryService.getFlightEntity(flight.getId())).thenReturn(flight);

        assertThatThrownBy(() -> paymentService.processPayment(requestDto))
                .isInstanceOf(FlightCanNotBeReservedException.class);
    }

    @DisplayName("Should fail when first-class capacity is exceeded")
    @Test
    void shouldFail_FirstClassCapacityExceeded() {
        FlightEntity flight = createFlight(FlightStatus.SCHEDULED, 100, 20);
        AddPassengerDto p1 = createAddPassengerDto("1001");
        PaymentPassengerDto pp1 = new PaymentPassengerDto(p1, SeatClass.FIRST_CLASS);
        PaymentRequestDto requestDto = new PaymentRequestDto(flight.getId(), "test@test.com", List.of(pp1));

        when(flightQueryService.getFlightEntity(flight.getId())).thenReturn(flight);
        // All 20 first-class seats already taken
        when(passengerReservationRepository.countByFlightAndSeatClassAndStatusNot(flight, SeatClass.FIRST_CLASS, PassengerReservationStatus.CANCELED)).thenReturn(20);

        assertThatThrownBy(() -> paymentService.processPayment(requestDto))
                .isInstanceOf(FlightCapacityExceededException.class);
    }

    @DisplayName("Should fail when economy capacity is exceeded")
    @Test
    void shouldFail_EconomyCapacityExceeded() {
        FlightEntity flight = createFlight(FlightStatus.SCHEDULED, 100, 20);
        AddPassengerDto p1 = createAddPassengerDto("1001");
        PaymentPassengerDto pp1 = new PaymentPassengerDto(p1, SeatClass.ECONOMY);
        PaymentRequestDto requestDto = new PaymentRequestDto(flight.getId(), "test@test.com", List.of(pp1));

        when(flightQueryService.getFlightEntity(flight.getId())).thenReturn(flight);
        when(passengerReservationRepository.countByFlightAndSeatClassAndStatusNot(flight, SeatClass.FIRST_CLASS, PassengerReservationStatus.CANCELED)).thenReturn(0);
        // All 100 economy seats already taken
        when(passengerReservationRepository.countByFlightAndSeatClassAndStatusNot(flight, SeatClass.ECONOMY, PassengerReservationStatus.CANCELED)).thenReturn(100);

        assertThatThrownBy(() -> paymentService.processPayment(requestDto))
                .isInstanceOf(FlightCapacityExceededException.class);
    }

    @DisplayName("Should fail when same passenger appears twice in the request")
    @Test
    void shouldFail_DuplicatedPassengers() {
        FlightEntity flight = createFlight(FlightStatus.SCHEDULED, 100, 20);
        AddPassengerDto p1 = createAddPassengerDto("1001");
        // Same passenger twice, different seat class
        PaymentPassengerDto pp1 = new PaymentPassengerDto(p1, SeatClass.ECONOMY);
        PaymentPassengerDto pp2 = new PaymentPassengerDto(p1, SeatClass.FIRST_CLASS);
        PaymentRequestDto requestDto = new PaymentRequestDto(flight.getId(), "test@test.com", List.of(pp1, pp2));

        when(flightQueryService.getFlightEntity(flight.getId())).thenReturn(flight);

        assertThatThrownBy(() -> paymentService.processPayment(requestDto))
                .isInstanceOf(DuplicatedPassengerException.class);
    }
}
