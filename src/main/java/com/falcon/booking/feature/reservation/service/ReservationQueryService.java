package com.falcon.booking.feature.reservation.service;

import com.falcon.booking.common.enums.ReservationStatus;
import com.falcon.booking.common.utils.StringNormalizer;
import com.falcon.booking.feature.flight.service.FlightQueryService;
import com.falcon.booking.feature.reservation.exception.ReservationNotFoundException;
import com.falcon.booking.feature.reservation.mapper.ReservationMapper;
import com.falcon.booking.persistence.entity.FlightEntity;
import com.falcon.booking.persistence.entity.PassengerEntity;
import com.falcon.booking.persistence.entity.PassengerReservationEntity;
import com.falcon.booking.persistence.entity.ReservationEntity;
import com.falcon.booking.persistence.repository.PassengerReservationRepository;
import com.falcon.booking.persistence.repository.ReservationRepository;
import com.falcon.booking.feature.passenger.service.PassengerService;
import com.falcon.booking.feature.reservation.dto.ResponseReservationDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReservationQueryService {

    private final ReservationRepository reservationRepository;
    private final PassengerReservationRepository passengerReservationRepository;
    private final FlightQueryService flightQueryService;
    private final PassengerService passengerService;
    private final ReservationMapper reservationMapper;

    public ReservationQueryService(ReservationRepository reservationRepository,
                                   PassengerReservationRepository passengerReservationRepository,
                                   FlightQueryService flightQueryService,
                                   PassengerService passengerService,
                                   ReservationMapper reservationMapper) {
        this.reservationRepository = reservationRepository;
        this.passengerReservationRepository = passengerReservationRepository;
        this.flightQueryService = flightQueryService;
        this.passengerService = passengerService;
        this.reservationMapper = reservationMapper;
    }

    public ReservationEntity getReservationEntityByNumber(String reservationNumber) {
        String normalized = StringNormalizer.normalize(reservationNumber);
        return reservationRepository.findByNumber(normalized)
                .orElseThrow(() -> new ReservationNotFoundException(normalized));
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
        FlightEntity flight = flightQueryService.getFlightEntity(flightId);
        Pageable pageable = PageRequest.of(page, size, Sort.by("reservationDatetime").ascending());
        return getAllReservationEntitiesActiveByFlight(flight, pageable).map(reservationMapper::toResponseDto);
    }
}
