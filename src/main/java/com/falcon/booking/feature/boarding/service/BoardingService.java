package com.falcon.booking.feature.boarding.service;

import com.falcon.booking.common.enums.PassengerReservationStatus;
import com.falcon.booking.feature.boarding.dto.BoardingPassValidationResponseDto;
import com.falcon.booking.feature.boarding.exception.BoardingPassNotFoundException;
import com.falcon.booking.feature.boarding.pdf.BoardingPassPdfGenerator;
import com.falcon.booking.feature.reservation.exception.PassengerReservationNotFoundException;
import com.falcon.booking.persistence.entity.*;
import com.falcon.booking.persistence.repository.BoardingPassRepository;
import com.falcon.booking.persistence.repository.PassengerReservationRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class BoardingService {

    private final PassengerReservationRepository passengerReservationRepository;
    private final BoardingPassRepository boardingPassRepository;
    private final BoardingPassPdfGenerator boardingPassPdfGenerator;
    private final BoardingEmailService boardingEmailService;
    private final BoardingPassCreationService boardingPassCreationService;

    public BoardingService(PassengerReservationRepository passengerReservationRepository,
                           BoardingPassRepository boardingPassRepository,
                           BoardingPassPdfGenerator boardingPassPdfGenerator,
                           BoardingEmailService boardingEmailService,
                           BoardingPassCreationService boardingPassCreationService) {
        this.passengerReservationRepository = passengerReservationRepository;
        this.boardingPassRepository = boardingPassRepository;
        this.boardingPassPdfGenerator = boardingPassPdfGenerator;
        this.boardingEmailService = boardingEmailService;
        this.boardingPassCreationService = boardingPassCreationService;
    }

    @Transactional
    public void issue(Long passengerReservationId) {
        try {
            log.info("Starting Boarding Pass issuance process for PassengerReservationId: {}", passengerReservationId);

            BoardingPassEntity boardingPass = boardingPassCreationService.createBoardingPass(passengerReservationId);
            boardingPass.markAsEmailed();

            boardingEmailService.generateAndSendBoardingPassEmail(passengerReservationId);

            log.info("Boarding pass successfully issued and emailed for PassengerReservationId: {}", passengerReservationId);
        } catch (Exception e) {
            log.error("Failed to generate and send boarding pass for PassengerReservationId: {}", passengerReservationId, e);
        }
    }

    @Transactional(readOnly = true)
    public byte[] generatePdf(Long passengerReservationId) {
        log.info("Generating Boarding Pass PDF for PassengerReservationId: {}", passengerReservationId);

        PassengerReservationEntity passengerReservation = passengerReservationRepository.findById(passengerReservationId)
                .orElseThrow(() -> new PassengerReservationNotFoundException(passengerReservationId));

        BoardingPassEntity boardingPass = boardingPassRepository.findByPassengerReservation(passengerReservation)
                .orElseThrow(() -> new BoardingPassNotFoundException(passengerReservationId));

        byte[] pdf = boardingPassPdfGenerator.generate(boardingPass).pdf();
        log.info("PDF generated successfully for PassengerReservationId: {} ({} bytes)", passengerReservationId, pdf.length);
        return pdf;
    }

    public BoardingPassValidationResponseDto validate(UUID qrToken) {
        BoardingPassEntity boardingPass = boardingPassRepository.findByQrToken(qrToken)
                .orElseThrow(() -> new BoardingPassNotFoundException(qrToken));

        PassengerReservationEntity passengerReservation = boardingPass.getPassengerReservation();
        PassengerEntity passenger = passengerReservation.getPassenger();
        FlightEntity flight = passengerReservation.getFlight();

        String seatLabel = flight.getAirplaneType().getSeatLabel(passengerReservation.getSeatNumber());

        return new BoardingPassValidationResponseDto(
                boardingPass.getQrToken(),
                passenger.getFullName(),
                passenger.getIdentification(),
                flight.getRoute().getFlightNumber(),
                flight.getRoute().getAirportOrigin().getCity(),
                flight.getRoute().getAirportDestination().getCity(),
                flight.getDepartureDateTime(),
                passengerReservation.getSeatClass(),
                passengerReservation.getSeatNumber(),
                seatLabel,
                boardingPass.getStatus()
        );
    }

    @Transactional
    public void boardPassenger(UUID qrToken) {
        BoardingPassEntity boardingPass = boardingPassRepository.findByQrToken(qrToken)
                .orElseThrow(() -> new BoardingPassNotFoundException(qrToken));

        PassengerReservationEntity passengerReservation = boardingPass.getPassengerReservation();
        passengerReservation.board();
        boardingPass.markAsBoarded();

        boardingPassRepository.save(boardingPass);
    }

    @Transactional
    public void expireUnusedPassesForFlight(Long flightId) {
        List<PassengerReservationEntity> unboardedReservations = passengerReservationRepository.findAllByFlightIdAndStatusIn(
                flightId, List.of(PassengerReservationStatus.RESERVED, PassengerReservationStatus.CHECKED_IN));

        for (PassengerReservationEntity reservation : unboardedReservations) {
            reservation.expire();
            boardingPassRepository.findByPassengerReservation(reservation).ifPresent(BoardingPassEntity::markAsExpired);
        }
        if (!unboardedReservations.isEmpty()) {
            log.info("{} reservation expired for flight: {}", unboardedReservations.size(), flightId);
       }
    }
}
