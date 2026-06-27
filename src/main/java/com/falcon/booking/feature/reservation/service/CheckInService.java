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
public class CheckInService {

    private static final Logger logger = LoggerFactory.getLogger(CheckInService.class);

    private final PassengerService passengerService;
    private final PassengerReservationMapper passengerReservationMapper;
    private final ReservationService reservationService;

    public CheckInService(PassengerService passengerService, PassengerReservationMapper passengerReservationMapper, ReservationService reservationService) {
        this.passengerService = passengerService;
        this.passengerReservationMapper = passengerReservationMapper;
        this.reservationService = reservationService;
    }


    @Transactional
    public ResponsePassengerReservationDto checkInByIdentificationNumber(String reservationNumber, String identificationNumber, String countryIsoCode) {
        PassengerEntity passenger = passengerService.getPassengerEntityByIdentificationNumber(identificationNumber, countryIsoCode);
        return passengerReservationMapper.toResponseDto(checkIn(reservationNumber, passenger));
    }

    public PassengerReservationEntity checkIn(String reservationNumber, PassengerEntity passenger) {
        ReservationEntity reservationEntity = reservationService.getReservationEntityByNumber(reservationNumber);
        logger.info("Passenger with id: {} has checked in for reservation {}", passenger.getId(), reservationEntity.getNumber());
        return reservationEntity.checkInPassenger(passenger);
    }

}
