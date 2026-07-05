package com.falcon.booking.feature.reservation.service;

import com.falcon.booking.feature.passenger.service.PassengerService;
import com.falcon.booking.feature.reservation.dto.ResponsePassengerReservationDto;
import com.falcon.booking.feature.reservation.mapper.PassengerReservationMapper;
import com.falcon.booking.persistence.entity.PassengerEntity;
import com.falcon.booking.persistence.entity.PassengerReservationEntity;
import com.falcon.booking.persistence.entity.ReservationEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BoardingService {

    private static final Logger logger = LoggerFactory.getLogger(BoardingService.class);

    private final PassengerService passengerService;
    private final PassengerReservationMapper passengerReservationMapper;
    private final ReservationQueryService reservationQueryService;

    public BoardingService(PassengerService passengerService, PassengerReservationMapper passengerReservationMapper, ReservationQueryService reservationQueryService) {
        this.passengerService = passengerService;
        this.passengerReservationMapper = passengerReservationMapper;
        this.reservationQueryService = reservationQueryService;
    }

    @Transactional
    public ResponsePassengerReservationDto boardByIdentificationNumber(String reservationNumber, String identificationNumber, String countryIsoCode) {
        PassengerEntity passenger = passengerService.getPassengerEntityByIdentificationNumber(identificationNumber, countryIsoCode);
        return passengerReservationMapper.toResponseDto(board(reservationNumber, passenger));
    }

    public PassengerReservationEntity board(String reservationNumber, PassengerEntity passenger) {
        ReservationEntity reservationEntity = reservationQueryService.getReservationEntityByNumber(reservationNumber);
        logger.info("Passenger with id: {} has boarded for reservation {}", passenger.getId(), reservationEntity.getNumber());
        return reservationEntity.board(passenger);
    }

}
