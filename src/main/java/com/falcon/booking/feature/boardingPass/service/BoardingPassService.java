package com.falcon.booking.feature.boardingPass.service;

import com.falcon.booking.common.email.EmailService;
import com.falcon.booking.common.email.dto.EmailAttachment;
import com.falcon.booking.common.email.dto.EmailInlineImage;
import com.falcon.booking.common.email.dto.EmailRequest;
import com.falcon.booking.common.email.template.EmailTemplateService;
import com.falcon.booking.feature.boardingPass.resources.ResourceService;
import com.falcon.booking.common.qr.QrCodeService;
import com.falcon.booking.feature.boardingPass.assembler.BoardingPassViewAssembler;
import com.falcon.booking.feature.boardingPass.dto.BoardingPassDocumentData;
import com.falcon.booking.feature.boardingPass.dto.BoardingPassView;
import com.falcon.booking.feature.boardingPass.pdf.BoardingPassPdfService;
import com.falcon.booking.feature.reservation.exception.PassengerReservationNotFoundException;
import com.falcon.booking.persistence.entity.*;
import com.falcon.booking.persistence.repository.BoardingPassRepository;
import com.falcon.booking.persistence.repository.PassengerReservationRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
public class BoardingPassService {

    @Value("${falcon.boarding-pass.validation-url}")
    private String validationUrl;

    @Value("${app.flight.boarding.minutes-before-to-close}")
    private int minutesBeforeToEndBoarding;

    @Value("${app.flight.boarding.minutes-before-to-start}")
    private int minutesBeforeToStartBoarding;

    private static final String LOGO_PATH = "static/images/falcon-logo.jpg";
    private static final String LOGO_CID = "falcon-logo";

    private final PassengerReservationRepository passengerReservationRepository;
    private final BoardingPassRepository boardingPassRepository;
    private final BoardingPassViewAssembler boardingPassViewAssembler;
    private final BoardingPassPdfService boardingPassPdfService;
    private final QrCodeService qrCodeService;
    private final EmailService emailService;
    private final EmailTemplateService emailTemplateService;
    private final byte[] logoBytes;

    public BoardingPassService(PassengerReservationRepository passengerReservationRepository, BoardingPassRepository boardingPassRepository, BoardingPassViewAssembler boardingPassViewAssembler, BoardingPassPdfService boardingPassPdfService, QrCodeService qrCodeService, EmailService emailService, EmailTemplateService emailTemplateService, ResourceService resourceService) {
        this.passengerReservationRepository = passengerReservationRepository;
        this.boardingPassRepository = boardingPassRepository;
        this.boardingPassViewAssembler = boardingPassViewAssembler;
        this.boardingPassPdfService = boardingPassPdfService;
        this.qrCodeService = qrCodeService;
        this.emailService = emailService;
        this.emailTemplateService = emailTemplateService;
        this.logoBytes = resourceService.loadAsBytes(LOGO_PATH);
    }

    @Async
    @Transactional
    public void issue(Long passengerReservationId) {
        try {
            log.info("Starting Boarding Pass issuance process for PassengerReservationId: {}", passengerReservationId);

            PassengerReservationEntity passengerReservation = passengerReservationRepository.findById(passengerReservationId)
                    .orElseThrow(() -> new PassengerReservationNotFoundException(passengerReservationId));

        BoardingPassEntity boardingPass = getOrCreateBoardingPass(passengerReservation);
        if (boardingPass.getId() == null) {
            log.debug("Creating new BoardingPassEntity for PassengerReservationId: {}", passengerReservation.getId());
            boardingPassRepository.save(boardingPass);
        } else {
            log.debug("Found existing BoardingPassEntity for PassengerReservationId: {}", passengerReservation.getId());
        }

        log.debug("Generating QR Code and Boarding Pass Document...");
        byte[] qr = qrCodeService.generate(buildValidationUrl(boardingPass));
        BoardingPassDocumentData document = buildDocument(passengerReservation, qr);
        BoardingPassView view = boardingPassViewAssembler.toView(document);

        byte[] pdf = boardingPassPdfService.generate(view);
        log.debug("PDF generation completed. Sending email to passenger...");

        sendEmail(passengerReservation, document, pdf);

            boardingPass.markAsEmailed();
            log.info("Boarding pass successfully issued and emailed for PassengerReservationId: {}", passengerReservationId);
        } catch (Exception e) {
            log.error("Failed to generate and send boarding pass for PassengerReservationId: {}", passengerReservationId, e);
        }
    }

    private BoardingPassEntity getOrCreateBoardingPass(PassengerReservationEntity passengerReservation) {
        return boardingPassRepository.findByPassengerReservation(passengerReservation)
                .orElseGet(() -> new BoardingPassEntity(passengerReservation, UUID.randomUUID()));

    }

    private void sendEmail(PassengerReservationEntity passengerReservation, BoardingPassDocumentData document, byte[] pdf) {
        log.info("Preparing to send Boarding Pass email to: {}", passengerReservation.getReservation().getContactEmail());
        FlightEntity flight = passengerReservation.getReservation().getFlight();

        String html = emailTemplateService.process(
                "email/boarding-pass-email",
                Map.of(
                        "passengerName", document.passengerName(),
                        "flightNumber", document.flightNumber(),
                        "originAirport", document.originAirport(),
                        "destinationAirport", document.destinationAirport(),
                        "departure", document.departureTime(),
                        "boardingStart", document.boardingStartTime(),
                        "boardingEnd", document.boardingEndTime(),
                        "seat", document.seat()
                )
        );

        EmailAttachment attachment = new EmailAttachment(
                "Falcon-Boarding-Pass-" + flight.getRoute().getFlightNumber() + ".pdf",
                "application/pdf",
                pdf
        );

        EmailInlineImage logoInline = new EmailInlineImage(LOGO_CID, "image/jpeg", logoBytes);

        EmailRequest request = new EmailRequest(
                passengerReservation
                        .getReservation()
                        .getContactEmail(),
                "Your Falcon Boarding Pass",
                html,
                true,
                List.of(logoInline),
                List.of(attachment)
        );

        emailService.send(request);
        log.info("Boarding Pass email request sent to EmailService successfully.");
    }

    private String buildValidationUrl(BoardingPassEntity boardingPass){
        return validationUrl + "/" + boardingPass.getQrToken();
    }

    private BoardingPassDocumentData buildDocument(PassengerReservationEntity passengerReservation, byte[] qr) {

        ReservationEntity reservation = passengerReservation.getReservation();
        PassengerEntity passenger = passengerReservation.getPassenger();
        FlightEntity flight = reservation.getFlight();

        OffsetDateTime departureDateTime = flight.getDepartureDateTime();


        LocalDateTime departureLocalDateTime = departureDateTime.atZoneSameInstant(ZoneId.of(flight.getRoute().getAirportOrigin().getTimezone())).toLocalDateTime();
        LocalDateTime boardingStartTime = departureLocalDateTime.minusMinutes(minutesBeforeToStartBoarding);
        LocalDateTime boardingEndTime = departureLocalDateTime.minusMinutes(minutesBeforeToEndBoarding);
        RouteEntity route = flight.getRoute();
        AirportEntity airportOrigin = route.getAirportOrigin();
        AirplaneTypeEntity airplaneType = flight.getAirplaneType();

        String seatLabel = airplaneType.getSeatLabel(passengerReservation.getSeatNumber());
        String zone = ZoneId.of(airportOrigin.getTimezone()).getDisplayName(TextStyle.SHORT, Locale.US);

        return new BoardingPassDocumentData(
                passenger.getFullName(),
                route.getFlightNumber(),
                route.getAirportOrigin().getIataCode(),
                route.getAirportDestination().getIataCode(),
                departureLocalDateTime,
                boardingStartTime,
                boardingEndTime,
                zone,
                seatLabel,
                reservation.getNumber(),
                qr
        );
    }
    private EmailAttachment buildPdfAttachment(byte[] pdf) {
        return new EmailAttachment("boarding-pass.pdf", "application/pdf", pdf);
    }
}