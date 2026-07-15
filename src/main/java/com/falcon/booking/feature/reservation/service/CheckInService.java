package com.falcon.booking.feature.reservation.service;

import com.falcon.booking.common.enums.PassengerReservationStatus;
import com.falcon.booking.feature.passenger.service.PassengerService;
import com.falcon.booking.feature.reservation.dto.ResponsePassengerReservationDto;
import com.falcon.booking.feature.reservation.exception.FlightCapacityExceededException;
import com.falcon.booking.feature.reservation.exception.SeatNumberAlreadyTakenException;
import com.falcon.booking.feature.reservation.exception.SeatNumberOutOfRangeException;
import com.falcon.booking.feature.reservation.mapper.PassengerReservationMapper;
import com.falcon.booking.persistence.entity.FlightEntity;
import com.falcon.booking.persistence.entity.PassengerEntity;
import com.falcon.booking.persistence.entity.PassengerReservationEntity;
import com.falcon.booking.persistence.entity.ReservationEntity;
import com.falcon.booking.persistence.repository.PassengerReservationRepository;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CheckInService {

    private static final Logger logger = LoggerFactory.getLogger(CheckInService.class);

    private final PassengerService passengerService;
    private final PassengerReservationMapper passengerReservationMapper;
    private final ReservationQueryService reservationQueryService;
    private final PassengerReservationRepository passengerReservationRepository;

    public CheckInService(PassengerService passengerService, PassengerReservationMapper passengerReservationMapper, ReservationQueryService reservationQueryService, PassengerReservationRepository passengerReservationRepository) {
        this.passengerService = passengerService;
        this.passengerReservationMapper = passengerReservationMapper;
        this.reservationQueryService = reservationQueryService;
        this.passengerReservationRepository = passengerReservationRepository;
    }


    @Transactional
    public ResponsePassengerReservationDto checkInByIdentificationNumber(String reservationNumber, String identificationNumber, String countryIsoCode, Integer seatNumber) {
        PassengerEntity passenger = passengerService.getPassengerEntityByIdentificationNumber(identificationNumber, countryIsoCode);
        return passengerReservationMapper.toResponseDto(checkIn(reservationNumber, passenger, seatNumber));
    }

    public PassengerReservationEntity checkIn(String reservationNumber, PassengerEntity passenger, Integer seatNumber) {
        ReservationEntity reservationEntity = reservationQueryService.getReservationEntityByNumber(reservationNumber);
        
        Integer finalSeat = validateOrGenerateSeatNumber(seatNumber, reservationEntity.getFlight());
        
        logger.info("Passenger with id: {} has checked in for reservation {}", passenger.getId(), reservationEntity.getNumber());
        return reservationEntity.checkInPassenger(passenger, finalSeat);
    }

    private Integer validateOrGenerateSeatNumber(Integer requestedSeatNumber, FlightEntity flight) {
        int maximumSeatNumber = flight.getAirplaneType().getTotalSeats();
        List<PassengerReservationEntity> allReservations = passengerReservationRepository.findAllByFlight(flight);
        Set<Integer> takenSeats = allReservations.stream()
                .filter(pr -> pr.getSeatNumber() != null && !pr.getStatus().equals(PassengerReservationStatus.CANCELED))
                .map(PassengerReservationEntity::getSeatNumber)
                .collect(Collectors.toSet());

        if (requestedSeatNumber != null) {
            if (requestedSeatNumber > maximumSeatNumber || requestedSeatNumber <= 0)
                throw new SeatNumberOutOfRangeException(requestedSeatNumber, maximumSeatNumber);
            if (takenSeats.contains(requestedSeatNumber))
                throw new SeatNumberAlreadyTakenException(requestedSeatNumber, flight.getId());
            return requestedSeatNumber;
        } else {
            for (int i = 1; i <= maximumSeatNumber; i++) {
                if (!takenSeats.contains(i)) {
                    return i; 
                }
            }
            throw new FlightCapacityExceededException(flight.getId());
        }
    }

}
