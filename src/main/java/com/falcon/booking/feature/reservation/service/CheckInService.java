package com.falcon.booking.feature.reservation.service;

import com.falcon.booking.common.enums.PassengerReservationStatus;
import com.falcon.booking.common.enums.SeatClass;
import com.falcon.booking.feature.passenger.service.PassengerService;
import com.falcon.booking.feature.reservation.dto.ResponsePassengerReservationDto;
import com.falcon.booking.feature.reservation.exception.*;
import com.falcon.booking.feature.reservation.mapper.PassengerReservationMapper;
import com.falcon.booking.persistence.entity.FlightEntity;
import com.falcon.booking.persistence.entity.PassengerEntity;
import com.falcon.booking.persistence.entity.PassengerReservationEntity;
import com.falcon.booking.persistence.entity.ReservationEntity;
import com.falcon.booking.persistence.repository.PassengerReservationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CheckInService {

    private static final Logger logger = LoggerFactory.getLogger(CheckInService.class);

    private final PassengerService passengerService;
    private final PassengerReservationMapper passengerReservationMapper;
    private final ReservationQueryService reservationQueryService;
    private final PassengerReservationRepository passengerReservationRepository;
    public CheckInService(PassengerService passengerService,
                          PassengerReservationMapper passengerReservationMapper,
                          ReservationQueryService reservationQueryService,
                          PassengerReservationRepository passengerReservationRepository) {
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

        PassengerReservationEntity passengerReservation = reservationEntity.getPassengerReservations().stream()
                .filter(pr -> pr.getPassenger().equals(passenger))
                .findFirst()
                .orElseThrow(() ->
                        new PassengerNotFoundInReservationException(passenger.getIdentificationNumber(), passenger.getCountryNationality().getIsoCode(), reservationNumber));

        SeatClass seatClass = passengerReservation.getSeatClass();

        if (!passengerReservation.isReserved()) {
            throw new InvalidCheckInPassengerReservationException(passengerReservation.getStatus());
        }

        List<PassengerReservationEntity> allReservations = passengerReservationRepository.findAllByFlight(reservationEntity.getFlight());

        Integer finalSeat = validateOrGenerateSeatNumber(seatNumber, reservationEntity.getFlight(), seatClass, allReservations);
        logger.info("Passenger with id: {} has checked in for reservation {}", passenger.getId(), reservationEntity.getNumber());
        PassengerReservationEntity finalPassengerReservation = reservationEntity.checkInPassenger(passenger, finalSeat);
        return finalPassengerReservation;
    }

    private Integer validateOrGenerateSeatNumber(Integer requestedSeatNumber, FlightEntity flight, SeatClass seatClass, List<PassengerReservationEntity> allReservations) {
        int firstClassSeats = flight.getAirplaneType().getFirstClassSeats();
        int totalSeats = flight.getAirplaneType().getTotalSeats();

        int minSeat = seatClass == SeatClass.FIRST_CLASS ? 1 : firstClassSeats + 1;
        int maxSeat = seatClass == SeatClass.FIRST_CLASS ? firstClassSeats : totalSeats;

        Set<Integer> takenSeats = allReservations.stream()
                .filter(pr -> pr.getSeatNumber() != null && !pr.getStatus().equals(PassengerReservationStatus.CANCELED))
                .map(PassengerReservationEntity::getSeatNumber)
                .collect(Collectors.toSet());

        if (requestedSeatNumber != null) {
            if (requestedSeatNumber < minSeat || requestedSeatNumber > maxSeat)
                throw new SeatNumberOutOfRangeException(requestedSeatNumber,minSeat, maxSeat);
            if (takenSeats.contains(requestedSeatNumber))
                throw new SeatNumberAlreadyTakenException(requestedSeatNumber, flight.getId());
            return requestedSeatNumber;
        } else {
            for (int i = minSeat; i <= maxSeat; i++) {
                if (!takenSeats.contains(i)) {
                    return i;
                }
            }
            throw new FlightCapacityExceededException(flight.getId());
        }
    }

    public List<PassengerReservationEntity> getPassengerReservationsByFlight(FlightEntity flight) {
        return passengerReservationRepository.findAllByFlight(flight);
    }
}
