package com.falcon.booking.feature.reservation.service;

import com.falcon.booking.feature.passenger.service.PassengerService;
import com.falcon.booking.feature.payment.dto.PaymentPassengerDto;
import com.falcon.booking.feature.payment.dto.PaymentRequestDto;
import com.falcon.booking.feature.reservation.component.ReservationNumberGenerator;
import com.falcon.booking.feature.reservation.dto.ResponseReservationDto;
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
import java.util.List;

@Service
public class ReservationCommandService {

    private static final Logger logger = LoggerFactory.getLogger(ReservationCommandService.class);

    private final ReservationRepository reservationRepository;
    private final PassengerReservationRepository passengerReservationRepository;
    private final PassengerService passengerService;
    private final ReservationMapper reservationMapper;
    private final ReservationQueryService reservationQueryService;
    private final ReservationNumberGenerator reservationNumberGenerator;

    public ReservationCommandService(ReservationRepository reservationRepository,
                                     PassengerReservationRepository passengerReservationRepository,
                                     PassengerService passengerService,
                                     ReservationMapper reservationMapper,
                                     ReservationQueryService reservationQueryService,
                                     ReservationNumberGenerator reservationNumberGenerator) {
        this.reservationRepository = reservationRepository;
        this.passengerReservationRepository = passengerReservationRepository;
        this.passengerService = passengerService;
        this.reservationMapper = reservationMapper;
        this.reservationQueryService = reservationQueryService;
        this.reservationNumberGenerator = reservationNumberGenerator;
    }

    public String createReservationFromPayment(PaymentRequestDto requestDto, FlightEntity flight) {
        String reservationNumber = reservationNumberGenerator.generate();
        ReservationEntity reservation = reservationRepository.save(new ReservationEntity(reservationNumber, flight, requestDto.contactEmail(), Instant.now()));

        List<PassengerReservationEntity> passengerReservations = new ArrayList<>();
        for (PaymentPassengerDto dto : requestDto.passengers()) {
            PassengerEntity passenger = passengerService.createOrGetPassenger(dto.getPassenger());
            PassengerReservationEntity pr = new PassengerReservationEntity(passenger, reservation, null, dto.getSeatClass());
            pr.setPrice(dto.getUnitPrice());
            passengerReservations.add(pr);
        }
        passengerReservationRepository.saveAll(passengerReservations);

        logger.info("Created reservation number {} for flight {} via payment. Passengers: {}", reservation.getNumber(), flight.getId(), passengerReservations.size());
        return reservationNumber;
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
