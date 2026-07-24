package com.falcon.booking.feature.payment.service;

import com.falcon.booking.common.enums.PassengerReservationStatus;
import com.falcon.booking.common.enums.PaymentStatus;
import com.falcon.booking.common.enums.SeatClass;
import com.falcon.booking.feature.flight.exception.FlightCanNotBeReservedException;
import com.falcon.booking.feature.flight.service.FlightQueryService;
import com.falcon.booking.feature.passenger.dto.AddPassengerDto;
import com.falcon.booking.feature.payment.dto.PaymentPassengerDto;
import com.falcon.booking.feature.payment.dto.PaymentRequestDto;
import com.falcon.booking.feature.payment.dto.ResponsePaymentDto;
import com.falcon.booking.feature.reservation.exception.DuplicatedPassengerException;
import com.falcon.booking.feature.reservation.exception.FlightCapacityExceededException;
import com.falcon.booking.feature.reservation.service.ReservationCommandService;
import com.falcon.booking.persistence.entity.FlightEntity;
import com.falcon.booking.persistence.entity.PaymentEntity;
import com.falcon.booking.persistence.repository.PassengerReservationRepository;
import com.falcon.booking.persistence.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class PaymentService {

    private final FlightQueryService flightQueryService;
    private final ReservationCommandService reservationCommandService;
    private final PaymentRepository paymentRepository;
    private final PassengerReservationRepository passengerReservationRepository;

    public PaymentService(FlightQueryService flightQueryService, ReservationCommandService reservationCommandService, PaymentRepository paymentRepository, PassengerReservationRepository passengerReservationRepository) {
        this.flightQueryService = flightQueryService;
        this.reservationCommandService = reservationCommandService;
        this.paymentRepository = paymentRepository;
        this.passengerReservationRepository = passengerReservationRepository;
    }

    @Transactional
    public ResponsePaymentDto processPayment(PaymentRequestDto requestDto) {
        FlightEntity flight = flightQueryService.getFlightEntity(requestDto.flightId());
        
        checkFlightCanBeReserved(flight);
        checkPassengerDuplication(requestDto.passengers());

        int currentFirstClass = passengerReservationRepository.countByFlightAndSeatClassAndStatusNot(flight, SeatClass.FIRST_CLASS, PassengerReservationStatus.CANCELED);
        int currentEconomy = passengerReservationRepository.countByFlightAndSeatClassAndStatusNot(flight, SeatClass.ECONOMY, com.falcon.booking.common.enums.PassengerReservationStatus.CANCELED);

        checkCapacityExceed(requestDto, flight, currentFirstClass, currentEconomy);

        BigDecimal totalAmount = BigDecimal.ZERO;
        BigDecimal priceFirstClass = flight.calculatePrice(SeatClass.FIRST_CLASS, currentFirstClass);
        BigDecimal priceEconomy = flight.calculatePrice(SeatClass.ECONOMY, currentEconomy);

        for (PaymentPassengerDto p : requestDto.passengers()) {
            BigDecimal price = (p.getSeatClass() == SeatClass.FIRST_CLASS) ? priceFirstClass : priceEconomy;
            p.setUnitPrice(price);
            totalAmount = totalAmount.add(price);
        }

        String reservationNumber = reservationCommandService.createReservationFromPayment(requestDto, flight);

        PaymentEntity payment = new PaymentEntity(reservationNumber, totalAmount, PaymentStatus.APPROVED, Instant.now());
        paymentRepository.save(payment);

        return new ResponsePaymentDto(reservationNumber, totalAmount, PaymentStatus.APPROVED, payment.getCreatedAt());
    }

    private void checkFlightCanBeReserved(FlightEntity flight) {
        if (!flight.canBeReserved()) {
            throw new FlightCanNotBeReservedException(flight.getId());
        }
    }

    private void checkCapacityExceed( PaymentRequestDto requestDto, FlightEntity flight, int currentFirstClass, int currentEconomy){
        long firstClassRequested = requestDto.passengers().stream()
                .filter(p -> p.getSeatClass() == SeatClass.FIRST_CLASS)
                .count();
        long economyRequested = requestDto.passengers().size() - firstClassRequested;

        if (firstClassRequested > 0 && (currentFirstClass + firstClassRequested > flight.getAirplaneType().getFirstClassSeats())) {
            throw new FlightCapacityExceededException(flight.getId());
        }

        if (economyRequested > 0 && (currentEconomy + economyRequested > flight.getAirplaneType().getEconomySeats())) {
            throw new FlightCapacityExceededException(flight.getId());
        }
    }

    private void checkPassengerDuplication(List<PaymentPassengerDto> passengers) {

        Set<String> identifications = new HashSet<>();

        passengers.stream()
                .map(PaymentPassengerDto::getPassenger)
                .map(AddPassengerDto::getIdentification)
                .filter(id -> !identifications.add(id))
                .findFirst()
                .ifPresent(id -> {throw new DuplicatedPassengerException(id);
                });
    }
}
