package com.falcon.booking.feature.reservation.service;

import com.falcon.booking.common.utils.StringNormalizer;
import com.falcon.booking.feature.flight.exception.FlightCanNotBeReservedException;
import com.falcon.booking.feature.reservation.exception.DuplicateSeatNumberInReservationException;
import com.falcon.booking.feature.reservation.exception.ReservationMustHavePassengersException;
import com.falcon.booking.feature.reservation.exception.SeatNumberAlreadyTakenException;
import com.falcon.booking.feature.reservation.exception.SeatNumberOutOfRangeException;
import com.falcon.booking.feature.reservation.exception.ReservationNotFoundException;
import com.falcon.booking.feature.flight.mapper.FlightMapper;
import com.falcon.booking.feature.flight.service.FlightService;
import com.falcon.booking.feature.passenger.service.PassengerService;
import com.falcon.booking.feature.reservation.mapper.PassengerReservationMapper;
import com.falcon.booking.feature.reservation.mapper.ReservationMapper;
import com.falcon.booking.common.enums.PassengerReservationStatus;
import com.falcon.booking.common.enums.ReservationStatus;
import com.falcon.booking.persistence.entity.FlightEntity;
import com.falcon.booking.persistence.entity.PassengerEntity;
import com.falcon.booking.persistence.entity.PassengerReservationEntity;
import com.falcon.booking.persistence.entity.ReservationEntity;
import com.falcon.booking.persistence.repository.PassengerReservationRepository;
import com.falcon.booking.persistence.repository.ReservationRepository;
import com.falcon.booking.feature.reservation.dto.AddPassengerReservationDto;
import com.falcon.booking.feature.reservation.dto.AddReservationDto;
import com.falcon.booking.feature.reservation.dto.ResponsePassengerReservationDto;
import com.falcon.booking.feature.reservation.dto.ResponseReservationDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class ReservationService {

    private static final Logger logger = LoggerFactory.getLogger(ReservationService.class);

    private final ReservationRepository reservationRepository;
    private final PassengerReservationRepository passengerReservationRepository;
    private final FlightService flightService;
    private final PassengerService passengerService;
    private final FlightMapper flightMapper;
    private final PassengerReservationMapper passengerReservationMapper;
    private final ReservationMapper reservationMapper;

    @Autowired
    public ReservationService(ReservationRepository reservationRepository, PassengerReservationRepository passengerReservationRepository, FlightService flightService, PassengerService passengerService, FlightMapper flightMapper, PassengerReservationMapper passengerReservationMapper, ReservationMapper reservationMapper) {
        this.reservationRepository = reservationRepository;
        this.passengerReservationRepository = passengerReservationRepository;
        this.flightService = flightService;
        this.passengerService = passengerService;
        this.flightMapper = flightMapper;
        this.passengerReservationMapper = passengerReservationMapper;
        this.reservationMapper = reservationMapper;
    }

    private String generateReservationNumber(){
        String alphabet = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        int reservationNumberLength = 6;
        StringBuilder reservationNumber;
        boolean alreadyExists;
        do{
            reservationNumber = new StringBuilder();
            for(int i = 0; i < reservationNumberLength; i++) {
                int index = ThreadLocalRandom.current().nextInt(0, alphabet.length());
                reservationNumber.append(alphabet.charAt(index));
            }
            alreadyExists = reservationRepository.existsByNumber(reservationNumber.toString());
        }while(alreadyExists);
        return reservationNumber.toString();
    }

    public ReservationEntity getReservationEntityByNumber(String reservationNumber) {
        String normalizedReservationNumber = StringNormalizer.normalize(reservationNumber);
        return reservationRepository.findByNumber(normalizedReservationNumber)
                .orElseThrow(()->new ReservationNotFoundException(normalizedReservationNumber));
    }

    public Page<ReservationEntity> getReservationsByPassenger(PassengerEntity passenger, Pageable pageable) {
        return passengerReservationRepository.findAllByPassenger(passenger, pageable)
                .map(PassengerReservationEntity::getReservation);
    }

    public Page<ReservationEntity> getAllReservationEntitiesActiveByFlight(FlightEntity flight, Pageable pageable) {
        return reservationRepository.findAllByFlightAndStatus(flight, ReservationStatus.RESERVED, pageable);
    }

    @Transactional(readOnly = true)
    public ResponseReservationDto getReservationByNumber(String reservationNumber) {
        return reservationMapper.toResponseDto(getReservationEntityByNumber(reservationNumber));
    }

    @Transactional(readOnly = true)
    public Page<ResponseReservationDto> getAllReservationsByPassengerIdentificationNumber(String identificationNumber, String countryIsoCode, int page, int size) {
        PassengerEntity passenger = passengerService.getPassengerEntityByIdentificationNumber(identificationNumber, countryIsoCode);
        Pageable pageable = PageRequest.of(page, size);
        return getReservationsByPassenger(passenger, pageable).map(reservationMapper::toResponseDto);
    }

    @Transactional(readOnly = true)
    public Page<ResponseReservationDto> getAllReservationsByFlight(Long flightId, int page, int size) {
        FlightEntity flight = flightService.getFlightEntity(flightId);
        Pageable pageable = PageRequest.of(page, size, Sort.by("reservationDatetime").ascending());
        return getAllReservationEntitiesActiveByFlight(flight, pageable).map(reservationMapper::toResponseDto);
    }

    @Transactional
    public ResponseReservationDto addReservation(AddReservationDto addReservationDto) {
        FlightEntity flightEntity = flightService.getFlightEntity(addReservationDto.idFlight());
        if(!flightEntity.canBeReserved())
            throw new FlightCanNotBeReservedException(flightEntity.getId());

        String reservationNumber = generateReservationNumber();
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

        if(addPassengersReservationsDto == null || addPassengersReservationsDto.isEmpty())
            throw new ReservationMustHavePassengersException();

        for (AddPassengerReservationDto addPassengerReservationDto : addPassengersReservationsDto) {
            if(seatNumbersRegistered.contains(addPassengerReservationDto.seatNumber()))
                throw new DuplicateSeatNumberInReservationException(addPassengerReservationDto.seatNumber());

            validateSeatNumber(addPassengerReservationDto.seatNumber(), reservation.getFlight());

            PassengerEntity passenger = passengerService.createOrGetPassenger(addPassengerReservationDto.passenger());
            PassengerReservationEntity passengerReservation =
                    new PassengerReservationEntity(passenger, reservation, addPassengerReservationDto.seatNumber());
            passengerReservations.add(passengerReservation);

            seatNumbersRegistered.add(passengerReservation.getSeatNumber());
        }
        return passengerReservations;
    }

    private void validateSeatNumber(Integer seatNumber, FlightEntity flight) {
        int maximumSeatNumber = flight.getAirplaneType().getTotalSeats();

        if(seatNumber > maximumSeatNumber || seatNumber <= 0)
            throw new SeatNumberOutOfRangeException(seatNumber, maximumSeatNumber);

        List<PassengerReservationEntity> seatReservations = passengerReservationRepository.findAllBySeatNumberAndFlight(seatNumber, flight);

        for(PassengerReservationEntity passengerReservation : seatReservations) {
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
        ReservationEntity reservation = getReservationEntityByNumber(reservationNumber);
        reservation.cancelPassenger(passenger);
        logger.info("Passenger with id {} canceled in reservation {}", passenger.getId(), reservation.getNumber());
        return reservation;
    }

    @Transactional
    public ResponseReservationDto cancelReservation(String reservationNumber) {
        ReservationEntity reservationEntity = getReservationEntityByNumber(reservationNumber);
        reservationEntity.cancel();
        logger.info("Reservation number {} has been canceled", reservationEntity.getNumber());
        return reservationMapper.toResponseDto(reservationEntity);
    }
/*
    @Transactional
    public ResponsePassengerReservationDto checkInByIdentificationNumber(String reservationNumber, String identificationNumber, String countryIsoCode) {
        PassengerEntity passenger = passengerService.getPassengerEntityByIdentificationNumber(identificationNumber, countryIsoCode);
        return passengerReservationMapper.toResponseDto(checkIn(reservationNumber, passenger));
    }

    public PassengerReservationEntity checkIn(String reservationNumber, PassengerEntity passenger) {
        ReservationEntity reservationEntity = getReservationEntityByNumber(reservationNumber);
        logger.info("Passenger with id: {} has checked in for reservation {}", passenger.getId(), reservationEntity.getNumber());
        return reservationEntity.checkInPassenger(passenger);
    }

    @Transactional
    public ResponsePassengerReservationDto boardByIdentificationNumber(String reservationNumber, String identificationNumber, String countryIsoCode) {
        PassengerEntity passenger = passengerService.getPassengerEntityByIdentificationNumber(identificationNumber, countryIsoCode);
        return passengerReservationMapper.toResponseDto(board(reservationNumber, passenger));
    }

    public PassengerReservationEntity board(String reservationNumber, PassengerEntity passenger) {
        ReservationEntity reservationEntity = getReservationEntityByNumber(reservationNumber);
        logger.info("Passenger with id: {} has boarded for reservation {}", passenger.getId(), reservationEntity.getNumber());
        return reservationEntity.board(passenger);
    }
*/
}

