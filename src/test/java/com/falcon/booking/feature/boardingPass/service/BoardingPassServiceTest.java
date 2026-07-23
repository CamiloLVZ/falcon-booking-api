package com.falcon.booking.feature.boardingPass.service;

import com.falcon.booking.common.email.EmailService;
import com.falcon.booking.common.email.dto.EmailRequest;
import com.falcon.booking.common.email.template.EmailTemplateService;
import com.falcon.booking.common.enums.SeatClass;
import com.falcon.booking.common.qr.QrCodeService;
import com.falcon.booking.feature.boardingPass.assembler.BoardingPassViewAssembler;
import com.falcon.booking.feature.boardingPass.dto.BoardingPassDocumentData;
import com.falcon.booking.feature.boardingPass.dto.BoardingPassView;
import com.falcon.booking.feature.boardingPass.pdf.BoardingPassPdfService;
import com.falcon.booking.feature.boardingPass.resources.ResourceService;
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
class BoardingPassServiceTest {

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

    private BoardingPassService boardingPassService;

    @BeforeEach
    void setUp() {
        when(resourceService.loadAsBytes(anyString())).thenReturn(new byte[]{1, 2, 3});
        boardingPassService = new BoardingPassService(
                passengerReservationRepository,
                boardingPassRepository,
                boardingPassViewAssembler,
                boardingPassPdfService,
                qrCodeService,
                emailService,
                emailTemplateService,
                resourceService
        );
        ReflectionTestUtils.setField(boardingPassService, "validationUrl", "https://falcon.example.com/boarding-pass/validate");
        ReflectionTestUtils.setField(boardingPassService, "minutesBeforeToEndBoarding", 15);
        ReflectionTestUtils.setField(boardingPassService, "minutesBeforeToStartBoarding", 60);
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

        boardingPassService.issue(10L);

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

        boardingPassService.issue(10L);

        // Email must always be sent regardless of whether the entity was new or preexisting
        verify(emailService).send(any(EmailRequest.class));
        verify(qrCodeService).generate(anyString());
        verify(boardingPassPdfService).generate(mockView);
    }

    @DisplayName("Should silently handle missing PassengerReservation without throwing")
    @Test
    void shouldHandleMissingPassengerReservation_Silently() {
        when(passengerReservationRepository.findById(10L)).thenReturn(Optional.empty());

        boardingPassService.issue(10L);

        verify(boardingPassRepository, never()).save(any());
        verify(emailService, never()).send(any());
    }
}
