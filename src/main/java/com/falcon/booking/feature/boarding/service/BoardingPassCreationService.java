package com.falcon.booking.feature.boarding.service;

import com.falcon.booking.feature.reservation.exception.PassengerReservationNotFoundException;
import com.falcon.booking.persistence.entity.BoardingPassEntity;
import com.falcon.booking.persistence.entity.PassengerReservationEntity;
import com.falcon.booking.persistence.repository.BoardingPassRepository;
import com.falcon.booking.persistence.repository.PassengerReservationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
public class BoardingPassCreationService {

    private final PassengerReservationRepository passengerReservationRepository;
    private final BoardingPassRepository boardingPassRepository;

    public BoardingPassCreationService(PassengerReservationRepository passengerReservationRepository,
                                       BoardingPassRepository boardingPassRepository) {
        this.passengerReservationRepository = passengerReservationRepository;
        this.boardingPassRepository = boardingPassRepository;
    }

    @Transactional
    public BoardingPassEntity createBoardingPass(Long passengerReservationId) {
        PassengerReservationEntity passengerReservation = passengerReservationRepository.findById(passengerReservationId)
                .orElseThrow(() -> new PassengerReservationNotFoundException(passengerReservationId));

        return boardingPassRepository.findByPassengerReservation(passengerReservation)
                .orElseGet(() -> {
                    log.debug("Creating new BoardingPassEntity for PassengerReservationId: {}", passengerReservation.getId());
                    return boardingPassRepository.save(new BoardingPassEntity(passengerReservation, UUID.randomUUID()));
                });
    }
}
