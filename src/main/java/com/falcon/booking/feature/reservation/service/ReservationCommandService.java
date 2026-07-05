package com.falcon.booking.feature.reservation.service;

import com.falcon.booking.common.enums.PassengerReservationStatus;
import com.falcon.booking.feature.flight.exception.FlightCanNotBeReservedException;
import com.falcon.booking.feature.flight.mapper.FlightMapper;
import com.falcon.booking.feature.flight.service.FlightQueryService;
import com.falcon.booking.feature.passenger.service.PassengerService;
import com.falcon.booking.feature.reservation.component.ReservationNumberGenerator;
import com.falcon.booking.feature.reservation.dto.AddPassengerReservationDto;
import com.falcon.booking.feature.reservation.dto.AddReservationDto;
import com.falcon.booking.feature.reservation.dto.ResponsePassengerReservationDto;
import com.falcon.booking.feature.reservation.dto.ResponseReservationDto;
import com.falcon.booking.feature.reservation.exception.DuplicateSeatNumberInReservationException;
import com.falcon.booking.feature.reservation.exception.ReservationMustHavePassengersException;
import com.falcon.booking.feature.reservation.exception.SeatNumberAlreadyTakenException;
import com.falcon.booking.feature.reservation.exception.SeatNumberOutOfRangeException;
import com.falcon.booking.feature.reservation.mapper.PassengerReservationMapper;
import com.falcon.booking.feature.reservation.mapper.ReservationMapper;
import com.falcon.booking.persistence.entity.FlightEntity;
import com.falcon.booking.persistence.entity.PassengerEntity;
import com.falcon.booking.persistence.entity.PassengerReservationEntity;
import com.falcon.booking.persistence.entity.ReservationEntity;
import com.falcon.booking.persistence.repository.PassengerReservationRepository;
import com.falcon.booking.persistence.repository.ReservationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class ReservationCommandService {

    private static final Logger logger = LoggerFactory.getLogger(ReservationCommandService.class);

    private final ReservationRepository reservationRepository;
    private final PassengerReservationRepository passengerReservationRepository;
    private final FlightQueryService flightQueryService;
    private final PassengerService passengerService;
    private final FlightMapper flightMapper;
    private final PassengerReservationMapper passengerReservationMapper;
    private final ReservationMapper reservationMapper;
    private final ReservationQueryService reservationQueryService;
    private final ReservationNumberGenerator reservationNumberGenerator;

    public ReservationCommandService(ReservationRepository reservationRepository,
                                     PassengerReservationRepository passengerReservationRepository,
                                     FlightQueryService flightQueryService,
                                     PassengerService passengerService,
                                     FlightMapper flightMapper,
                                     PassengerReservationMapper passengerReservationMapper,
                                     ReservationMapper reservationMapper,
                                     ReservationQueryService reservationQueryService,
                                     ReservationNumberGenerator reservationNumberGenerator) {
        this.reservationRepository = reservationRepository;
        this.passengerReservationRepository = passengerReservationRepository;
        this.flightQueryService = flightQueryService;
        this.passengerService = passengerService;
        this.flightMapper = flightMapper;
        this.passengerReservationMapper = passengerReservationMapper;
        this.reservationMapper = reservationMapper;
        this.reservationQueryService = reservationQueryService;
        this.reservationNumberGenerator = reservationNumberGenerator;
    }

    @Transactional
    public ResponseReservationDto addReservation(AddReservationDto addReservationDto) {
        FlightEntity flightEntity = flightQueryService.getFlightEntity(addReservationDto.idFlight());
        if (!flightEntity.canBeReserved())
            throw new FlightCanNotBeReservedException(flightEntity.getId());

        String reservationNumber = reservationNumberGenerator.generate();
        ReservationEntity reservation = reservationRepository.save(
                new ReservationEntity(reservationNumber, flightEntity, addReservationDto.contactEmail(), Instant.now()));

        List<PassengerReservationEntity> passengerReservations = createPassengerReservationEntities(addReservationDto.passengers(), reservation);
        List<ResponsePassengerReservationDto> responsePassengerReservationDtos = passengerReservationMapper.toResponseDto(passengerReservationRepository.saveAll(passengerReservations));

        logger.info("Created reservation number {} for flight {}. Passengers: {}", reservation.getNumber(), flightEntity.getId(), passengerReservations.size());
        return new ResponseReservationDto(reservation.getNumber(), reservation.getContactEmail(),
                reservation.getReservationDatetime(), reservation.getStatus(), flightMapper.toDto(flightEntity), responsePassengerReservationDtos);
    }

    private List<PassengerReservationEntity> createPassengerReservationEntities(List<AddPassengerReservationDto> addPassengersReservationsDto, ReservationEntity reservation) {
        List<PassengerReservationEntity> passengerReservations = new ArrayList<>();
        Set<Integer> seatNumbersRegistered = new HashSet<>();

        if (addPassengersReservationsDto == null || addPassengersReservationsDto.isEmpty())
            throw new ReservationMustHavePassengersException();

        for (AddPassengerReservationDto dto : addPassengersReservationsDto) {
            if (seatNumbersRegistered.contains(dto.seatNumber()))
                throw new DuplicateSeatNumberInReservationException(dto.seatNumber());

            validateSeatNumber(dto.seatNumber(), reservation.getFlight());

            PassengerEntity passenger = passengerService.createOrGetPassenger(dto.passenger());
            PassengerReservationEntity passengerReservation = new PassengerReservationEntity(passenger, reservation, dto.seatNumber());
            passengerReservations.add(passengerReservation);
            seatNumbersRegistered.add(passengerReservation.getSeatNumber());
        }
        return passengerReservations;
    }

    private void validateSeatNumber(Integer seatNumber, FlightEntity flight) {
        int maximumSeatNumber = flight.getAirplaneType().getTotalSeats();
        if (seatNumber > maximumSeatNumber || seatNumber <= 0)
            throw new SeatNumberOutOfRangeException(seatNumber, maximumSeatNumber);

        List<PassengerReservationEntity> seatReservations = passengerReservationRepository.findAllBySeatNumberAndFlight(seatNumber, flight);
        for (PassengerReservationEntity passengerReservation : seatReservations) {
            if (passengerReservation != null) {
                if (!passengerReservation.getStatus().equals(PassengerReservationStatus.CANCELED))
                    throw new SeatNumberAlreadyTakenException(seatNumber, flight.getId());
            }
        }
    }

    @Transactional
    public ResponseReservationDto cancelPassengerReservationByIdentificationNumber(String reservationNumber, String identificationNumber, String countryIsoCode) {
        PassengerEntity passenger = passengerService.getPassengerEntityByIdentificationNumber(identificationNumber, countryIsoCode);
        return reservationMapper.toResponseDto(cancelPassengerReservation(reservationNumber, passenger));
    }

    @Transactional
    public ResponseReservationDto cancelPassengerReservationByPassportNumber(String reservationNumber, String passportNumber) {
        PassengerEntity passenger = passengerService.getPassengerEntityByPassportNumber(passportNumber);
        return reservationMapper.toResponseDto(cancelPassengerReservation(reservationNumber, passenger));
    }

    public ReservationEntity cancelPassengerReservation(String reservationNumber, PassengerEntity passenger) {
        ReservationEntity reservation = reservationQueryService.getReservationEntityByNumber(reservationNumber);
        reservation.cancelPassenger(passenger);
        logger.info("Passenger with id {} canceled in reservation {}", passenger.getId(), reservation.getNumber());
        return reservation;
    }

    @Transactional
    public ResponseReservationDto cancelReservation(String reservationNumber) {
        ReservationEntity reservationEntity = reservationQueryService.getReservationEntityByNumber(reservationNumber);
        reservationEntity.cancel();
        logger.info("Reservation number {} has been canceled", reservationEntity.getNumber());
        return reservationMapper.toResponseDto(reservationEntity);
    }
}
