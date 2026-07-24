package com.falcon.booking.feature.boarding.service;

import com.falcon.booking.common.email.EmailService;
import com.falcon.booking.common.email.dto.EmailRequest;
import com.falcon.booking.common.email.template.EmailTemplateService;
import com.falcon.booking.common.enums.SeatClass;
import com.falcon.booking.common.qr.QrCodeService;
import com.falcon.booking.feature.boarding.assembler.BoardingPassViewAssembler;
import com.falcon.booking.feature.boarding.dto.BoardingPassDocumentData;
import com.falcon.booking.feature.boarding.dto.BoardingPassView;
import com.falcon.booking.feature.boarding.pdf.BoardingPassPdfService;
import com.falcon.booking.feature.boarding.resources.ResourceService;
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
    private BoardingPassViewAssembler boardingPassViewAssembler;
    @Mock
    private BoardingPassPdfService boardingPassPdfService;
    @Mock
    private QrCodeService qrCodeService;
    @Mock
    private EmailService emailService;
    @Mock
    private EmailTemplateService emailTemplateService;
    @Mock
    private ResourceService resourceService;

    private BoardingService boardingService;

    @BeforeEach
    void setUp() {
        when(resourceService.loadAsBytes(anyString())).thenReturn(new byte[]{1, 2, 3});
        boardingService = new BoardingService(
                passengerReservationRepository,
                boardingPassRepository,
                boardingPassViewAssembler,
                boardingPassPdfService,
                qrCodeService,
                emailService,
                emailTemplateService,
                resourceService
        );
        ReflectionTestUtils.setField(boardingService, "baseUrl", "https://falcon.example.com");
        ReflectionTestUtils.setField(boardingService, "contextPath", "/api");
        ReflectionTestUtils.setField(boardingService, "validationPath", "/v1/boarding-passes");
        ReflectionTestUtils.setField(boardingService, "minutesBeforeToEndBoarding", 15);
        ReflectionTestUtils.setField(boardingService, "minutesBeforeToStartBoarding", 60);
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

    @DisplayName("Should issue boarding pass: generate QR, PDF, save entity and send email")
    @Test
    void shouldIssueBoardingPass() {
        PassengerReservationEntity pr = buildPassengerReservation();
        BoardingPassView mockView = mock(BoardingPassView.class);

        when(passengerReservationRepository.findById(10L)).thenReturn(Optional.of(pr));
        when(boardingPassRepository.findByPassengerReservation(pr)).thenReturn(Optional.empty());
        when(boardingPassRepository.save(any(BoardingPassEntity.class))).thenAnswer(i -> i.getArguments()[0]);
        when(qrCodeService.generate(anyString())).thenReturn(new byte[]{4, 5, 6});
        when(boardingPassViewAssembler.toView(any(BoardingPassDocumentData.class))).thenReturn(mockView);
        when(boardingPassPdfService.generate(mockView)).thenReturn(new byte[]{7, 8, 9});
        when(emailTemplateService.process(anyString(), any())).thenReturn("<html>email</html>");

        boardingService.issue(10L);

        verify(passengerReservationRepository).findById(10L);
        verify(qrCodeService).generate(anyString());
        verify(boardingPassPdfService).generate(mockView);
        verify(boardingPassRepository).save(any(BoardingPassEntity.class));
        verify(emailService).send(any(EmailRequest.class));
    }

    @DisplayName("Should find existing boarding pass entity via repository and still send email")
    @Test
    void shouldFindExistingBoardingPassEntityAndSendEmail() {
        PassengerReservationEntity pr = buildPassengerReservation();
        BoardingPassEntity existingPass = new BoardingPassEntity(pr, UUID.randomUUID());
        BoardingPassView mockView = mock(BoardingPassView.class);

        when(passengerReservationRepository.findById(10L)).thenReturn(Optional.of(pr));
        when(boardingPassRepository.findByPassengerReservation(pr)).thenReturn(Optional.of(existingPass));
        when(qrCodeService.generate(anyString())).thenReturn(new byte[]{4, 5, 6});
        when(boardingPassViewAssembler.toView(any(BoardingPassDocumentData.class))).thenReturn(mockView);
        when(boardingPassPdfService.generate(mockView)).thenReturn(new byte[]{7, 8, 9});
        when(emailTemplateService.process(anyString(), any())).thenReturn("<html>email</html>");

        boardingService.issue(10L);

        // Email must always be sent regardless of whether the entity was new or preexisting
        verify(emailService).send(any(EmailRequest.class));
        verify(qrCodeService).generate(anyString());
        verify(boardingPassPdfService).generate(mockView);
    }

    @DisplayName("Should silently handle missing PassengerReservation without throwing")
    @Test
    void shouldHandleMissingPassengerReservation_Silently() {
        when(passengerReservationRepository.findById(10L)).thenReturn(Optional.empty());

        boardingService.issue(10L);

        verify(boardingPassRepository, never()).save(any());
        verify(emailService, never()).send(any());
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
}
