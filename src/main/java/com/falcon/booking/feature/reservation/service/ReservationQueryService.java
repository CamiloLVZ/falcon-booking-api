package com.falcon.booking.feature.reservation.service;

import com.falcon.booking.common.enums.ReservationStatus;
import com.falcon.booking.common.utils.StringNormalizer;
import com.falcon.booking.feature.flight.service.FlightQueryService;
import com.falcon.booking.feature.passenger.service.PassengerService;
import com.falcon.booking.feature.reservation.dto.ResponsePassengerReservationDto;
import com.falcon.booking.feature.reservation.dto.ResponseReservationDto;
import com.falcon.booking.feature.reservation.exception.ReservationNotFoundException;
import com.falcon.booking.feature.reservation.mapper.PassengerReservationMapper;
import com.falcon.booking.feature.reservation.mapper.ReservationMapper;
import com.falcon.booking.persistence.entity.FlightEntity;
import com.falcon.booking.persistence.entity.PassengerEntity;
import com.falcon.booking.persistence.entity.PassengerReservationEntity;
import com.falcon.booking.persistence.entity.ReservationEntity;
import com.falcon.booking.persistence.entity.UserEntity;
import com.falcon.booking.persistence.repository.PassengerReservationRepository;
import com.falcon.booking.persistence.repository.ReservationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ReservationQueryService {

    private final ReservationRepository reservationRepository;
    private final PassengerReservationRepository passengerReservationRepository;
    private final FlightQueryService flightQueryService;
    private final PassengerService passengerService;
    private final ReservationMapper reservationMapper;
    private final PassengerReservationMapper passengerReservationMapper;

    public ReservationQueryService(ReservationRepository reservationRepository,
                                   PassengerReservationRepository passengerReservationRepository,
                                   FlightQueryService flightQueryService,
                                   PassengerService passengerService,
                                   ReservationMapper reservationMapper,
                                   PassengerReservationMapper passengerReservationMapper) {
        this.reservationRepository = reservationRepository;
        this.passengerReservationRepository = passengerReservationRepository;
        this.flightQueryService = flightQueryService;
        this.passengerService = passengerService;
        this.reservationMapper = reservationMapper;
        this.passengerReservationMapper = passengerReservationMapper;
    }

    public ReservationEntity getReservationEntityByNumber(String reservationNumber) {
        String normalized = StringNormalizer.normalize(reservationNumber);
        return reservationRepository.findByNumber(normalized)
                .orElseThrow(() -> new ReservationNotFoundException(normalized));
    }

    public ReservationEntity getReservationEntityById(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new ReservationNotFoundException(id));
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
    public ResponseReservationDto getReservationById(Long id) {
        return reservationMapper.toResponseDto(getReservationEntityById(id));
    }

    @Transactional(readOnly = true)
    public List<ResponsePassengerReservationDto> getPassengerReservationsSummary(Long passengerId) {
        PassengerEntity passenger = passengerService.getPassengerEntityById(passengerId);
        Pageable topThree = PageRequest.of(0, 3);
        List<PassengerReservationEntity> passengerReservations =
                passengerReservationRepository.findTop3ByPassengerOrderByReservationDatetimeDesc(passenger, topThree);
        return passengerReservationMapper.toResponseDto(passengerReservations);
    }

    @Transactional(readOnly = true)
    public Page<ResponseReservationDto> getAllReservationsByPassengerIdentificationNumber(String identificationNumber, String countryIsoCode, int page, int size) {
        PassengerEntity passenger = passengerService.getPassengerEntityByIdentificationNumber(identificationNumber, countryIsoCode);
        Pageable pageable = PageRequest.of(page, size);
        return getReservationsByPassenger(passenger, pageable).map(reservationMapper::toResponseDto);
    }

    @Transactional(readOnly = true)
    public Page<ResponseReservationDto> getAllReservationsByFlight(Long flightId, int page, int size) {
        FlightEntity flight = flightQueryService.getFlightEntity(flightId);
        Pageable pageable = PageRequest.of(page, size, Sort.by("reservationDatetime").ascending());
        return getAllReservationEntitiesActiveByFlight(flight, pageable).map(reservationMapper::toResponseDto);
    }

    @Transactional(readOnly = true)
    public Page<ResponseReservationDto> getMyReservations(UserEntity user, ReservationStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("reservationDatetime").descending());
        if (status != null) {
            return reservationRepository.findAllByUserAndStatus(user, status, pageable).map(reservationMapper::toResponseDto);
        }
        return reservationRepository.findAllByUser(user, pageable).map(reservationMapper::toResponseDto);
    }
}
