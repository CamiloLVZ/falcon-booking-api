package com.falcon.booking.integration;

import com.falcon.booking.common.enums.*;
import com.falcon.booking.feature.passenger.dto.AddPassengerDto;
import com.falcon.booking.feature.payment.dto.FlightPriceQuoteDto;
import com.falcon.booking.feature.payment.dto.PaymentPassengerDto;
import com.falcon.booking.feature.payment.dto.PaymentRequestDto;
import com.falcon.booking.feature.payment.dto.ResponsePaymentDto;
import com.falcon.booking.feature.reservation.dto.ReservationAccessRequestDto;
import com.falcon.booking.feature.reservation.dto.ResponseReservationDto;
import com.falcon.booking.persistence.entity.*;
import com.falcon.booking.persistence.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class BookingFlowIT extends BaseIntegrationTest {

    private static final AtomicInteger counter = new AtomicInteger(0);

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

    private Long flightId;

    @BeforeEach
    void setUp() {
        CountryEntity co = countryRepository.findByIsoCode("CO").orElseThrow();
        AirportEntity bog = airportRepository.findByIataCode("BOG").orElseThrow();
        AirportEntity mde = airportRepository.findByIataCode("MDE").orElseThrow();

        int id = counter.incrementAndGet();

        AirplaneTypeEntity plane = new AirplaneTypeEntity();
        plane.setProducer("Airbus");
        plane.setModel("A320-TEST-" + id);
        plane.configureSeats(150, 0, "ABC");
        plane.setStatus(AirplaneTypeStatus.ACTIVE);
        plane = airplaneTypeRepository.save(plane);

        RouteEntity route = new RouteEntity();
        route.setFlightNumber("AV" + (100 + id));
        route.setAirportOrigin(bog);
        route.setAirportDestination(mde);
        route.setDefaultAirplaneType(plane);
        route.setDurationMinutes(60);
        route.setBasePriceEconomy(new BigDecimal("100.00"));
        route.setBasePriceFirstClass(new BigDecimal("200.00"));
        route.setStatus(RouteStatus.ACTIVE);
        route.getRouteDays().add(new RouteDayEntity(route, DayOfWeek.MONDAY));
        route.getRouteSchedules().add(new RouteScheduleEntity(route, LocalTime.of(10, 0)));
        route = routeRepository.save(route);

        FlightEntity flight = new FlightEntity(route, plane, OffsetDateTime.now().plusDays(30), FlightStatus.SCHEDULED);
        flight.setBasePriceEconomy(new BigDecimal("100.00"));
        flight.setBasePriceFirstClass(new BigDecimal("200.00"));
        flight = flightRepository.save(flight);

        flightId = flight.getId();
    }

    @Test
    @DisplayName("Should get price quote for an existing flight")
    void shouldGetPriceQuoteForExistingFlight() {
        ResponseEntity<FlightPriceQuoteDto> response = restTemplate.getForEntity(
                baseUrl() + "/v1/flights/{id}/quote", FlightPriceQuoteDto.class, flightId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().flightId()).isEqualTo(flightId);
        assertThat(response.getBody().priceEconomy()).isGreaterThan(BigDecimal.ZERO);
        assertThat(response.getBody().priceFirstClass()).isGreaterThan(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Should return 404 when quoting a non-existent flight")
    void shouldReturn404ForNonExistentFlightQuote() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                baseUrl() + "/v1/flights/{id}/quote", String.class, 99999L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("Should process payment and create reservation")
    void shouldCreateReservationAndReturnApprovedPayment() {
        AddPassengerDto passenger = new AddPassengerDto(
                "Juan", "Perez", PassengerGender.M, "CO",
                LocalDate.of(1995, 7, 16), "A1234567", "1032456789"
        );
        PaymentPassengerDto paymentPassenger = new PaymentPassengerDto(passenger, SeatClass.ECONOMY);
        PaymentRequestDto request = new PaymentRequestDto(flightId, "contact@test.com", List.of(paymentPassenger));

        ResponseEntity<ResponsePaymentDto> response = restTemplate.postForEntity(
                baseUrl() + "/v1/payments", request, ResponsePaymentDto.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().reservationNumber()).isNotBlank();
        assertThat(response.getBody().status()).isEqualTo(PaymentStatus.APPROVED);
        assertThat(response.getBody().totalAmount()).isGreaterThan(BigDecimal.ZERO);
        assertThat(response.getBody().processedAt()).isNotNull();
    }

    @Test
    @DisplayName("Should return 400 when payment request has missing fields")
    void shouldReturn400ForInvalidPaymentRequest() {
        AddPassengerDto passenger = new AddPassengerDto(
                "Juan", "Perez", PassengerGender.M, "CO",
                LocalDate.of(1995, 7, 16), "A1234567", "1032456789"
        );
        PaymentPassengerDto paymentPassenger = new PaymentPassengerDto(passenger, SeatClass.ECONOMY);
        PaymentRequestDto request = new PaymentRequestDto(null, "", List.of(paymentPassenger));

        ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl() + "/v1/payments", request, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("Should return 404 when paying for a non-existent flight")
    void shouldReturn404ForNonExistentFlightPayment() {
        AddPassengerDto passenger = new AddPassengerDto(
                "Juan", "Perez", PassengerGender.M, "CO",
                LocalDate.of(1995, 7, 16), "A1234567", "1032456789"
        );
        PaymentPassengerDto paymentPassenger = new PaymentPassengerDto(passenger, SeatClass.ECONOMY);
        PaymentRequestDto request = new PaymentRequestDto(99999L, "contact@test.com", List.of(paymentPassenger));

        ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl() + "/v1/payments", request, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("Full flow: create reservation and retrieve it by number")
    void reservationLifecycle() {
        AddPassengerDto passenger = new AddPassengerDto(
                "Maria", "Gomez", PassengerGender.F, "CO",
                LocalDate.of(1990, 3, 10), "B7654321", "1098765432"
        );
        PaymentPassengerDto paymentPassenger = new PaymentPassengerDto(passenger, SeatClass.ECONOMY);
        PaymentRequestDto request = new PaymentRequestDto(flightId, "maria@test.com", List.of(paymentPassenger));

        ResponseEntity<ResponsePaymentDto> paymentResponse = restTemplate.postForEntity(
                baseUrl() + "/v1/payments", request, ResponsePaymentDto.class);

        assertThat(paymentResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        String reservationNumber = paymentResponse.getBody().reservationNumber();

        ResponseEntity<ResponseReservationDto> getResponse = restTemplate.getForEntity(
                baseUrl() + "/v1/reservations/{number}?contactEmail={email}",
                ResponseReservationDto.class, reservationNumber, "maria@test.com");

        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getResponse.getBody()).isNotNull();
        assertThat(getResponse.getBody().number()).isEqualTo(reservationNumber);
        assertThat(getResponse.getBody().status()).isEqualTo(ReservationStatus.RESERVED);
        assertThat(getResponse.getBody().contactEmail()).isEqualTo("maria@test.com");
        assertThat(getResponse.getBody().passengers()).hasSize(1);
    }

    @Test
    @DisplayName("Should cancel a reservation")
    void shouldCancelReservation() {
        AddPassengerDto passenger = new AddPassengerDto(
                "Carlos", "Lopez", PassengerGender.M, "CO",
                LocalDate.of(1985, 12, 25), "C1122334", "1044455667"
        );
        PaymentPassengerDto paymentPassenger = new PaymentPassengerDto(passenger, SeatClass.ECONOMY);
        PaymentRequestDto request = new PaymentRequestDto(flightId, "carlos@test.com", List.of(paymentPassenger));

        ResponseEntity<ResponsePaymentDto> paymentResponse = restTemplate.postForEntity(
                baseUrl() + "/v1/payments", request, ResponsePaymentDto.class);

        String reservationNumber = paymentResponse.getBody().reservationNumber();

        var cancelBody = new ReservationAccessRequestDto("carlos@test.com");
        ResponseEntity<ResponseReservationDto> cancelResponse = restTemplate.exchange(
                baseUrl() + "/v1/reservations/{number}/cancel",
                HttpMethod.PATCH,
                new HttpEntity<>(cancelBody),
                ResponseReservationDto.class,
                reservationNumber);

        assertThat(cancelResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(cancelResponse.getBody()).isNotNull();
        assertThat(cancelResponse.getBody().status()).isEqualTo(ReservationStatus.CANCELED);
    }
}
