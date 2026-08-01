package com.falcon.booking.feature.boarding.service;

import com.falcon.booking.common.enums.SeatClass;
import com.falcon.booking.feature.boarding.dto.BoardingPassDocumentData;
import com.falcon.booking.feature.boarding.pdf.BoardingPassPdfGenerator;
import com.falcon.booking.feature.boarding.pdf.BoardingPassPdfResult;
import com.falcon.booking.persistence.entity.*;
import com.falcon.booking.persistence.repository.BoardingPassRepository;
import com.falcon.booking.persistence.repository.PassengerReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BoardingServiceTest {

    @Mock
    private PassengerReservationRepository passengerReservationRepository;
    @Mock
    private BoardingPassRepository boardingPassRepository;
    @Mock
    private BoardingPassPdfGenerator boardingPassPdfGenerator;
    @Mock
    private BoardingEmailService boardingEmailService;

    private BoardingService boardingService;

    @BeforeEach
    void setUp() {
        boardingService = new BoardingService(
                passengerReservationRepository,
                boardingPassRepository,
                boardingPassPdfGenerator,
                boardingEmailService
        );
    }

    private PassengerReservationEntity buildPassengerReservation() {
        CountryEntity country = new CountryEntity();
        country.setIsoCode("CO");

        AirportEntity origin = new AirportEntity();
        origin.setIataCode("BOG");
        origin.setTimezone("America/Bogota");

        AirportEntity destination = new AirportEntity();
        destination.setIataCode("MDE");

        RouteEntity route = new RouteEntity();
        route.setAirportOrigin(origin);
        route.setAirportDestination(destination);
        route.setFlightNumber("AV1234");

        AirplaneTypeEntity airplaneType = new AirplaneTypeEntity();
        airplaneType.configureSeats(108, 12, "ABCDEF");

        FlightEntity flight = new FlightEntity();
        flight.setId(1L);
        flight.setRoute(route);
        flight.setAirplaneType(airplaneType);
        flight.setDepartureDateTime(OffsetDateTime.now().plusHours(12));

        PassengerEntity passenger = new PassengerEntity();
        passenger.setId(1L);
        passenger.setFirstName("Ana");
        passenger.setLastName("Perez");
        passenger.setIdentificationNumber("12345");

        // Use the public constructor: ReservationEntity(number, flight, contactEmail, datetime)
        ReservationEntity reservation = new ReservationEntity("RES123", flight, "test@test.com", Instant.now());

        return new PassengerReservationEntity(passenger, reservation, 5, SeatClass.ECONOMY);
    }

    @DisplayName("Should issue boarding pass: save entity, mark as emailed and delegate async email")
    @Test
    void shouldIssueBoardingPass() {
        PassengerReservationEntity pr = buildPassengerReservation();

        when(passengerReservationRepository.findById(10L)).thenReturn(Optional.of(pr));
        when(boardingPassRepository.findByPassengerReservation(pr)).thenReturn(Optional.empty());
        when(boardingPassRepository.save(any(BoardingPassEntity.class))).thenAnswer(i -> i.getArguments()[0]);

        boardingService.issue(10L);

        verify(passengerReservationRepository).findById(10L);
        verify(boardingPassRepository).save(any(BoardingPassEntity.class));
        verify(boardingEmailService).generateAndSendBoardingPassEmail(10L);
    }

    @DisplayName("Should find existing boarding pass entity via repository and still delegate async email")
    @Test
    void shouldFindExistingBoardingPassEntityAndSendEmail() {
        PassengerReservationEntity pr = buildPassengerReservation();
        BoardingPassEntity existingPass = new BoardingPassEntity(pr, UUID.randomUUID());

        when(passengerReservationRepository.findById(10L)).thenReturn(Optional.of(pr));
        when(boardingPassRepository.findByPassengerReservation(pr)).thenReturn(Optional.of(existingPass));

        boardingService.issue(10L);

        // Email must always be delegated regardless of whether the entity was new or preexisting
        verify(boardingEmailService).generateAndSendBoardingPassEmail(10L);
        verify(boardingPassRepository, never()).save(any());
    }

    @DisplayName("Should silently handle missing PassengerReservation without throwing")
    @Test
    void shouldHandleMissingPassengerReservation_Silently() {
        when(passengerReservationRepository.findById(10L)).thenReturn(Optional.empty());

        boardingService.issue(10L);

        verify(boardingPassRepository, never()).save(any());
        verify(boardingEmailService, never()).generateAndSendBoardingPassEmail(anyLong());
    }

    @DisplayName("Should validate boarding pass successfully")
    @Test
    void shouldValidateBoardingPass() {
        PassengerReservationEntity pr = buildPassengerReservation();
        PassengerEntity passenger = pr.getPassenger();
        CountryEntity country = new CountryEntity();
        ReflectionTestUtils.setField(country, "isoCode", "CO");
        ReflectionTestUtils.setField(passenger, "countryNationality", country);
        UUID token = UUID.randomUUID();
        BoardingPassEntity boardingPass = new BoardingPassEntity(pr, token);
        when(boardingPassRepository.findByQrToken(token)).thenReturn(Optional.of(boardingPass));

        com.falcon.booking.feature.boarding.dto.BoardingPassValidationResponseDto response = boardingService.validate(token);

        org.junit.jupiter.api.Assertions.assertNotNull(response);
        org.junit.jupiter.api.Assertions.assertEquals(token, response.qrToken());
        org.junit.jupiter.api.Assertions.assertEquals("ANA PEREZ", response.passengerName());
    }

    @DisplayName("Should fail validate if boarding pass not found")
    @Test
    void shouldFailValidate_IfNotFound() {
        UUID token = UUID.randomUUID();
        when(boardingPassRepository.findByQrToken(token)).thenReturn(Optional.empty());

        org.junit.jupiter.api.Assertions.assertThrows(
                com.falcon.booking.feature.boarding.exception.BoardingPassNotFoundException.class,
                () -> boardingService.validate(token)
        );
    }

    @DisplayName("Should board passenger successfully")
    @Test
    void shouldBoardPassenger() {
        PassengerReservationEntity pr = buildPassengerReservation();
        ReflectionTestUtils.setField(pr, "status", com.falcon.booking.common.enums.PassengerReservationStatus.CHECKED_IN);
        FlightEntity flight = pr.getFlight();
        UUID token = UUID.randomUUID();
        BoardingPassEntity boardingPass = new BoardingPassEntity(pr, token);
        // Make sure it can be boarded (flight must be BOARDING)
        ReflectionTestUtils.setField(flight, "status", com.falcon.booking.common.enums.FlightStatus.BOARDING);
        when(boardingPassRepository.findByQrToken(token)).thenReturn(Optional.of(boardingPass));

        boardingService.boardPassenger(token);

        verify(boardingPassRepository).save(boardingPass);
        org.junit.jupiter.api.Assertions.assertEquals(com.falcon.booking.common.enums.BoardingPassStatus.BOARDED, boardingPass.getStatus());
        org.junit.jupiter.api.Assertions.assertEquals(com.falcon.booking.common.enums.PassengerReservationStatus.BOARDED, pr.getStatus());
    }

    @DisplayName("Should expire unused passes and passenger reservations for flight")
    @Test
    void shouldExpireUnusedPassesForFlight() {
        PassengerReservationEntity pr = buildPassengerReservation();
        BoardingPassEntity boardingPass = new BoardingPassEntity(pr, UUID.randomUUID());
        Long flightId = 1L;

        when(passengerReservationRepository.findAllByFlightIdAndStatusIn(eq(flightId), anyList()))
                .thenReturn(java.util.List.of(pr));
        when(boardingPassRepository.findByPassengerReservation(pr)).thenReturn(Optional.of(boardingPass));

        boardingService.expireUnusedPassesForFlight(flightId);

        org.junit.jupiter.api.Assertions.assertTrue(pr.isExpired());
        org.junit.jupiter.api.Assertions.assertEquals(com.falcon.booking.common.enums.BoardingPassStatus.EXPIRED, boardingPass.getStatus());
    }

    @DisplayName("Should generate PDF for existing boarding pass")
    @Test
    void shouldGeneratePdf() {
        PassengerReservationEntity pr = buildPassengerReservation();
        BoardingPassEntity existingPass = new BoardingPassEntity(pr, UUID.randomUUID());
        BoardingPassDocumentData document = new BoardingPassDocumentData(
                "ANA PEREZ", "AV1234", "BOG", "MDE",
                LocalDateTime.of(2026, 8, 1, 10, 0),
                LocalDateTime.of(2026, 8, 1, 9, 0),
                LocalDateTime.of(2026, 8, 1, 9, 45),
                "GMT-5", "12A", "RES123", new byte[]{4, 5, 6});
        byte[] expectedPdf = {7, 8, 9};

        when(passengerReservationRepository.findById(10L)).thenReturn(Optional.of(pr));
        when(boardingPassRepository.findByPassengerReservation(pr)).thenReturn(Optional.of(existingPass));
        when(boardingPassPdfGenerator.generate(existingPass)).thenReturn(new BoardingPassPdfResult(document, expectedPdf));

        byte[] result = boardingService.generatePdf(10L);

        org.junit.jupiter.api.Assertions.assertArrayEquals(expectedPdf, result);
        verify(passengerReservationRepository).findById(10L);
        verify(boardingPassRepository).findByPassengerReservation(pr);
        verify(boardingPassPdfGenerator).generate(existingPass);
        verify(boardingPassRepository, never()).save(any());
        verify(boardingEmailService, never()).generateAndSendBoardingPassEmail(anyLong());
    }

    @DisplayName("Should throw BoardingPassNotFoundException when generating PDF if boarding pass was not created yet")
    @Test
    void shouldThrowBoardingPassNotFoundException_whenBoardingPassDoesNotExist_generatePdf() {
        PassengerReservationEntity pr = buildPassengerReservation();
        when(passengerReservationRepository.findById(10L)).thenReturn(Optional.of(pr));
        when(boardingPassRepository.findByPassengerReservation(pr)).thenReturn(Optional.empty());

        org.junit.jupiter.api.Assertions.assertThrows(
                com.falcon.booking.feature.boarding.exception.BoardingPassNotFoundException.class,
                () -> boardingService.generatePdf(10L)
        );
    }

    @DisplayName("Should throw PassengerReservationNotFoundException when generating PDF for non-existent reservation")
    @Test
    void shouldThrowException_whenPassengerReservationNotFound_generatePdf() {
        when(passengerReservationRepository.findById(10L)).thenReturn(Optional.empty());

        org.junit.jupiter.api.Assertions.assertThrows(
                com.falcon.booking.feature.reservation.exception.PassengerReservationNotFoundException.class,
                () -> boardingService.generatePdf(10L)
        );
    }

    @DisplayName("Should create boarding pass synchronously if not present")
    @Test
    void shouldCreateBoardingPass_whenNotPresent() {
        PassengerReservationEntity pr = buildPassengerReservation();
        when(passengerReservationRepository.findById(10L)).thenReturn(Optional.of(pr));
        when(boardingPassRepository.findByPassengerReservation(pr)).thenReturn(Optional.empty());
        when(boardingPassRepository.save(any(BoardingPassEntity.class))).thenAnswer(i -> i.getArguments()[0]);

        BoardingPassEntity created = boardingService.createBoardingPass(10L);

        org.junit.jupiter.api.Assertions.assertNotNull(created);
        verify(boardingPassRepository).save(any(BoardingPassEntity.class));
    }

    @DisplayName("Should return existing boarding pass when calling createBoardingPass")
    @Test
    void shouldReturnExistingBoardingPass_whenCreateBoardingPassCalled() {
        PassengerReservationEntity pr = buildPassengerReservation();
        BoardingPassEntity existingPass = new BoardingPassEntity(pr, UUID.randomUUID());
        when(passengerReservationRepository.findById(10L)).thenReturn(Optional.of(pr));
        when(boardingPassRepository.findByPassengerReservation(pr)).thenReturn(Optional.of(existingPass));

        BoardingPassEntity result = boardingService.createBoardingPass(10L);

        org.junit.jupiter.api.Assertions.assertEquals(existingPass, result);
        verify(boardingPassRepository, never()).save(any());
    }
}
