package com.falcon.booking.feature.boarding.service;

import com.falcon.booking.common.email.EmailService;
import com.falcon.booking.common.email.dto.EmailAttachment;
import com.falcon.booking.common.email.dto.EmailInlineImage;
import com.falcon.booking.common.email.dto.EmailRequest;
import com.falcon.booking.common.email.template.EmailTemplateService;
import com.falcon.booking.common.enums.SeatClass;
import com.falcon.booking.feature.boarding.dto.BoardingPassDocumentData;
import com.falcon.booking.feature.boarding.pdf.BoardingPassPdfGenerator;
import com.falcon.booking.feature.boarding.pdf.BoardingPassPdfResult;
import com.falcon.booking.feature.boarding.resources.ResourceService;
import com.falcon.booking.persistence.entity.AirplaneTypeEntity;
import com.falcon.booking.persistence.entity.AirportEntity;
import com.falcon.booking.persistence.entity.BoardingPassEntity;
import com.falcon.booking.persistence.entity.FlightEntity;
import com.falcon.booking.persistence.entity.PassengerEntity;
import com.falcon.booking.persistence.entity.PassengerReservationEntity;
import com.falcon.booking.persistence.entity.ReservationEntity;
import com.falcon.booking.persistence.entity.RouteEntity;
import com.falcon.booking.persistence.repository.BoardingPassRepository;
import com.falcon.booking.persistence.repository.PassengerReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BoardingEmailServiceTest {

    @Mock
    private PassengerReservationRepository passengerReservationRepository;
    @Mock
    private BoardingPassRepository boardingPassRepository;
    @Mock
    private BoardingPassPdfGenerator boardingPassPdfGenerator;
    @Mock
    private EmailService emailService;
    @Mock
    private EmailTemplateService emailTemplateService;
    @Mock
    private ResourceService resourceService;

    private BoardingEmailService boardingEmailService;

    @BeforeEach
    void setUp() {
        when(resourceService.loadAsBytes(anyString())).thenReturn(new byte[]{1, 2, 3});
        boardingEmailService = new BoardingEmailService(
                passengerReservationRepository,
                boardingPassRepository,
                boardingPassPdfGenerator,
                emailService,
                emailTemplateService,
                resourceService
        );
    }

    private PassengerReservationEntity buildPassengerReservation() {
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

        ReservationEntity reservation = new ReservationEntity("RES123", flight, "test@test.com", Instant.now());

        return new PassengerReservationEntity(passenger, reservation, 5, SeatClass.ECONOMY);
    }

    private BoardingPassDocumentData buildDocument() {
        return new BoardingPassDocumentData(
                "ANA PEREZ",
                "AV1234",
                "BOG",
                "MDE",
                LocalDateTime.of(2026, 8, 1, 10, 0),
                LocalDateTime.of(2026, 8, 1, 9, 0),
                LocalDateTime.of(2026, 8, 1, 9, 45),
                "GMT-5",
                "12A",
                "RES123",
                new byte[]{4, 5, 6}
        );
    }

    @DisplayName("Should generate QR/PDF and send boarding pass email with attachment and inline logo")
    @Test
    void shouldGenerateAndSendBoardingPassEmail() {
        PassengerReservationEntity pr = buildPassengerReservation();
        BoardingPassEntity boardingPass = new BoardingPassEntity(pr, UUID.randomUUID());
        BoardingPassDocumentData document = buildDocument();
        byte[] pdf = {7, 8, 9};

        when(passengerReservationRepository.findById(10L)).thenReturn(Optional.of(pr));
        when(boardingPassRepository.findByPassengerReservation(pr)).thenReturn(Optional.of(boardingPass));
        when(boardingPassPdfGenerator.generate(boardingPass)).thenReturn(new BoardingPassPdfResult(document, pdf));
        when(emailTemplateService.process(anyString(), anyMap())).thenReturn("<html>boarding pass</html>");

        boardingEmailService.generateAndSendBoardingPassEmail(10L);

        verify(boardingPassPdfGenerator).generate(boardingPass);
        verify(emailTemplateService).process(eq("email/boarding-pass-email"), anyMap());

        ArgumentCaptor<EmailRequest> captor = ArgumentCaptor.forClass(EmailRequest.class);
        verify(emailService).send(captor.capture());
        EmailRequest request = captor.getValue();

        assertEquals("test@test.com", request.to());
        assertEquals("Your Falcon Boarding Pass", request.subject());
        assertEquals("<html>boarding pass</html>", request.body());
        assertTrue(request.html());

        assertEquals(1, request.attachments().size());
        EmailAttachment attachment = request.attachments().get(0);
        assertEquals("Falcon-Boarding-Pass-AV1234.pdf", attachment.fileName());
        assertEquals("application/pdf", attachment.contentType());
        assertArrayEquals(pdf, attachment.data());

        assertEquals(1, request.inlineImages().size());
        EmailInlineImage logo = request.inlineImages().get(0);
        assertEquals("falcon-logo", logo.contentId());
        assertEquals("image/jpeg", logo.contentType());
        assertArrayEquals(new byte[]{1, 2, 3}, logo.data());
    }

    @DisplayName("Should handle email sending failure without propagating the exception")
    @Test
    void shouldHandleEmailFailureSilently() {
        PassengerReservationEntity pr = buildPassengerReservation();
        BoardingPassEntity boardingPass = new BoardingPassEntity(pr, UUID.randomUUID());
        BoardingPassDocumentData document = buildDocument();

        when(passengerReservationRepository.findById(10L)).thenReturn(Optional.of(pr));
        when(boardingPassRepository.findByPassengerReservation(pr)).thenReturn(Optional.of(boardingPass));
        when(boardingPassPdfGenerator.generate(boardingPass)).thenReturn(new BoardingPassPdfResult(document, new byte[]{7, 8, 9}));
        when(emailTemplateService.process(anyString(), anyMap())).thenReturn("<html>boarding pass</html>");
        doThrow(new RuntimeException("smtp down")).when(emailService).send(any(EmailRequest.class));

        assertDoesNotThrow(() -> boardingEmailService.generateAndSendBoardingPassEmail(10L));
        verify(emailService).send(any(EmailRequest.class));
    }

    @DisplayName("Should skip email when boarding pass was not created yet")
    @Test
    void shouldSkipEmailWhenBoardingPassNotFound() {
        PassengerReservationEntity pr = buildPassengerReservation();

        when(passengerReservationRepository.findById(10L)).thenReturn(Optional.of(pr));
        when(boardingPassRepository.findByPassengerReservation(pr)).thenReturn(Optional.empty());

        assertDoesNotThrow(() -> boardingEmailService.generateAndSendBoardingPassEmail(10L));
        verify(boardingPassPdfGenerator, never()).generate(any());
        verify(emailService, never()).send(any());
    }
}
