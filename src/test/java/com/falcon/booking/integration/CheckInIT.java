package com.falcon.booking.integration;

import com.falcon.booking.common.enums.AirplaneTypeStatus;
import com.falcon.booking.common.enums.BoardingPassStatus;
import com.falcon.booking.common.enums.FlightStatus;
import com.falcon.booking.common.enums.PassengerGender;
import com.falcon.booking.common.enums.PassengerReservationStatus;
import com.falcon.booking.common.enums.RouteStatus;
import com.falcon.booking.common.enums.SeatClass;
import com.falcon.booking.feature.boarding.dto.BoardingPassValidationResponseDto;
import com.falcon.booking.feature.checkIn.dto.CheckInRequestDto;
import com.falcon.booking.feature.passenger.dto.AddPassengerDto;
import com.falcon.booking.feature.payment.dto.PaymentPassengerDto;
import com.falcon.booking.feature.payment.dto.PaymentRequestDto;
import com.falcon.booking.feature.payment.dto.ResponsePaymentDto;
import com.falcon.booking.feature.reservation.dto.ResponsePassengerReservationDto;
import com.falcon.booking.persistence.entity.AirplaneTypeEntity;
import com.falcon.booking.persistence.entity.AirportEntity;
import com.falcon.booking.persistence.entity.BoardingPassEntity;
import com.falcon.booking.persistence.entity.CountryEntity;
import com.falcon.booking.persistence.entity.FlightEntity;
import com.falcon.booking.persistence.entity.PassengerReservationEntity;
import com.falcon.booking.persistence.entity.RouteDayEntity;
import com.falcon.booking.persistence.entity.RouteEntity;
import com.falcon.booking.persistence.entity.RouteScheduleEntity;
import com.falcon.booking.persistence.repository.AirplaneTypeRepository;
import com.falcon.booking.persistence.repository.AirportRepository;
import com.falcon.booking.persistence.repository.BoardingPassRepository;
import com.falcon.booking.persistence.repository.CountryRepository;
import com.falcon.booking.persistence.repository.FlightRepository;
import com.falcon.booking.persistence.repository.PassengerReservationRepository;
import com.falcon.booking.persistence.repository.RouteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class CheckInIT extends BaseIntegrationTest {

    private static final AtomicInteger counter = new AtomicInteger(0);

    @Autowired private CountryRepository countryRepository;
    @Autowired private AirportRepository airportRepository;
    @Autowired private AirplaneTypeRepository airplaneTypeRepository;
    @Autowired private RouteRepository routeRepository;
    @Autowired private FlightRepository flightRepository;
    @Autowired private BoardingPassRepository boardingPassRepository;
    @Autowired private PassengerReservationRepository passengerReservationRepository;

    private Long flightId;
    private String reservationNumber;
    private String contactEmail = "checkin-ci@test.com";
    private AddPassengerDto passenger;
    private String identificationNumber;

    @BeforeEach
    void setUp() {
        CountryEntity co = countryRepository.findByIsoCode("CO").orElseThrow();
        AirportEntity bog = airportRepository.findByIataCode("BOG").orElseThrow();
        AirportEntity mde = airportRepository.findByIataCode("MDE").orElseThrow();

        int id = counter.incrementAndGet();

        AirplaneTypeEntity plane = new AirplaneTypeEntity();
        plane.setProducer("Airbus");
        plane.setModel("A320-CI-" + id);
        plane.configureSeats(150, 0, "ABC");
        plane.setStatus(AirplaneTypeStatus.ACTIVE);
        plane = airplaneTypeRepository.save(plane);

        RouteEntity route = new RouteEntity();
        route.setFlightNumber("CI" + (100 + id));
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

        flight.startCheckIn();
        flight = flightRepository.save(flight);

        flightId = flight.getId();

        passenger = new AddPassengerDto(
                "Carlos", "CheckIn", PassengerGender.M, "CO",
                LocalDate.of(1990, 5, 15), "CI123456", "3098765432"
        );
        identificationNumber = passenger.identificationNumber();

        PaymentPassengerDto pp = new PaymentPassengerDto(passenger, SeatClass.ECONOMY);
        PaymentRequestDto paymentReq = new PaymentRequestDto(flightId, contactEmail, List.of(pp));

        ResponseEntity<ResponsePaymentDto> paymentResponse = restTemplate.postForEntity(
                baseUrl() + "/v1/payments", paymentReq, ResponsePaymentDto.class);
        reservationNumber = paymentResponse.getBody().reservationNumber();
    }

    @Test
    @DisplayName("Should check in a passenger successfully")
    void checkIn_Successful_ShouldReturn200() {
        var checkInReq = new CheckInRequestDto(reservationNumber, contactEmail, identificationNumber, "CO", null);

        ResponseEntity<ResponsePassengerReservationDto> response = restTemplate.postForEntity(
                baseUrl() + "/v1/check-in", checkInReq, ResponsePassengerReservationDto.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(PassengerReservationStatus.CHECKED_IN);
        assertThat(response.getBody().seatNumber()).isNotNull();
    }

    @Test
    @DisplayName("Should return 404 when check-in with invalid reservation")
    void checkIn_InvalidReservation_ShouldReturn404() {
        var checkInReq = new CheckInRequestDto("INVALID123", contactEmail, identificationNumber, "CO", null);

        ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl() + "/v1/check-in", checkInReq, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("Should return 400 when checking in already checked-in passenger")
    void checkIn_AlreadyCheckedIn_ShouldReturn400() {
        var checkInReq = new CheckInRequestDto(reservationNumber, contactEmail, identificationNumber, "CO", null);

        restTemplate.postForEntity(baseUrl() + "/v1/check-in", checkInReq, ResponsePassengerReservationDto.class);

        ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl() + "/v1/check-in", checkInReq, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("Should validate and board a passenger via boarding pass QR token")
    void boardPassenger_ValidToken_ShouldReturn200() throws Exception {
        var checkInReq = new CheckInRequestDto(reservationNumber, contactEmail, identificationNumber, "CO", null);
        ResponseEntity<ResponsePassengerReservationDto> checkInResponse = restTemplate.postForEntity(
                baseUrl() + "/v1/check-in", checkInReq, ResponsePassengerReservationDto.class);

        Long passengerReservationId = checkInResponse.getBody().id();

        FlightEntity flight = flightRepository.findById(flightId).orElseThrow();
        flight.startBoarding();
        flightRepository.save(flight);

        PassengerReservationEntity pr = passengerReservationRepository.findById(passengerReservationId).orElseThrow();

        Optional<BoardingPassEntity> bpOpt = Optional.empty();
        for (int i = 0; i < 20; i++) {
            bpOpt = boardingPassRepository.findByPassengerReservation(pr);
            if (bpOpt.isPresent()) break;
            Thread.sleep(500);
        }

        UUID qrToken = bpOpt.orElseThrow().getQrToken();

        ResponseEntity<BoardingPassValidationResponseDto> validationResponse = restTemplate.getForEntity(
                baseUrl() + "/v1/boarding-passes/{qrToken}", BoardingPassValidationResponseDto.class, qrToken);

        assertThat(validationResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(validationResponse.getBody()).isNotNull();
        assertThat(validationResponse.getBody().qrToken()).isEqualTo(qrToken);
        assertThat(validationResponse.getBody().status()).isEqualTo(BoardingPassStatus.ISSUED);

        ResponseEntity<String> boardResponse = restTemplate.exchange(
                baseUrl() + "/v1/boarding-passes/board/{qrToken}",
                HttpMethod.PATCH,
                null,
                String.class,
                qrToken);

        assertThat(boardResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}
