package com.falcon.booking.feature.boarding.service;

import com.falcon.booking.common.email.EmailService;
import com.falcon.booking.common.email.dto.EmailAttachment;
import com.falcon.booking.common.email.dto.EmailInlineImage;
import com.falcon.booking.common.email.dto.EmailRequest;
import com.falcon.booking.common.email.template.EmailTemplateService;
import com.falcon.booking.feature.boarding.dto.BoardingPassDocumentData;
import com.falcon.booking.feature.boarding.exception.BoardingPassNotFoundException;
import com.falcon.booking.feature.boarding.pdf.BoardingPassPdfGenerator;
import com.falcon.booking.feature.boarding.pdf.BoardingPassPdfResult;
import com.falcon.booking.feature.boarding.resources.ResourceService;
import com.falcon.booking.feature.reservation.exception.PassengerReservationNotFoundException;
import com.falcon.booking.persistence.entity.BoardingPassEntity;
import com.falcon.booking.persistence.entity.PassengerReservationEntity;
import com.falcon.booking.persistence.repository.BoardingPassRepository;
import com.falcon.booking.persistence.repository.PassengerReservationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class BoardingEmailService {

    private static final String LOGO_PATH = "static/images/falcon-logo.jpg";
    private static final String LOGO_CID = "falcon-logo";

    private final PassengerReservationRepository passengerReservationRepository;
    private final BoardingPassRepository boardingPassRepository;
    private final BoardingPassPdfGenerator boardingPassPdfGenerator;
    private final EmailService emailService;
    private final EmailTemplateService emailTemplateService;
    private final byte[] logoBytes;

    public BoardingEmailService(PassengerReservationRepository passengerReservationRepository,
                                BoardingPassRepository boardingPassRepository,
                                BoardingPassPdfGenerator boardingPassPdfGenerator,
                                @Qualifier("resendEmailService") EmailService emailService,
                                EmailTemplateService emailTemplateService,
                                ResourceService resourceService) {
        this.passengerReservationRepository = passengerReservationRepository;
        this.boardingPassRepository = boardingPassRepository;
        this.boardingPassPdfGenerator = boardingPassPdfGenerator;
        this.emailService = emailService;
        this.emailTemplateService = emailTemplateService;
        this.logoBytes = resourceService.loadAsBytes(LOGO_PATH);
    }

    @Async("boardingExecutor")
    @Transactional(readOnly = true)
    public void generateAndSendBoardingPassEmail(Long passengerReservationId) {
        try {
            log.info("Starting async Boarding Pass email generation for PassengerReservationId: {}", passengerReservationId);

            PassengerReservationEntity passengerReservation = passengerReservationRepository.findById(passengerReservationId)
                    .orElseThrow(() -> new PassengerReservationNotFoundException(passengerReservationId));

            BoardingPassEntity boardingPass = boardingPassRepository.findByPassengerReservation(passengerReservation)
                    .orElseThrow(() -> new BoardingPassNotFoundException(passengerReservationId));

            BoardingPassPdfResult result = boardingPassPdfGenerator.generate(boardingPass);

            sendEmail(passengerReservation, result.document(), result.pdf());
            log.info("Boarding pass email sent successfully for PassengerReservationId: {}", passengerReservationId);
        } catch (Exception e) {
            log.error("Failed to generate and send boarding pass email for PassengerReservationId: {}", passengerReservationId, e);
        }
    }

    private void sendEmail(PassengerReservationEntity passengerReservation, BoardingPassDocumentData document, byte[] pdf) {
        log.info("Preparing to send Boarding Pass email to: {}", passengerReservation.getReservation().getContactEmail());

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
                "Falcon-Boarding-Pass-" + document.flightNumber() + ".pdf",
                "application/pdf",
                pdf
        );

        EmailInlineImage logoInline = new EmailInlineImage(LOGO_CID, "image/jpeg", logoBytes);

        EmailRequest request = new EmailRequest(
                passengerReservation.getReservation().getContactEmail(),
                "Your Falcon Boarding Pass",
                html,
                true,
                List.of(logoInline),
                List.of(attachment)
        );

        emailService.send(request);
        log.info("Boarding Pass email request sent to EmailService successfully.");
    }
}
