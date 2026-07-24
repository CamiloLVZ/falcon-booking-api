package com.falcon.booking.feature.payment.service;

import com.falcon.booking.common.enums.PassengerReservationStatus;
import com.falcon.booking.common.enums.SeatClass;
import com.falcon.booking.feature.flight.service.FlightQueryService;
import com.falcon.booking.feature.payment.dto.FlightPriceQuoteDto;
import com.falcon.booking.persistence.entity.FlightEntity;
import com.falcon.booking.persistence.repository.PassengerReservationRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class PricingService {

    private final FlightQueryService flightQueryService;
    private final PassengerReservationRepository passengerReservationRepository;

    public PricingService(FlightQueryService flightQueryService, PassengerReservationRepository passengerReservationRepository) {
        this.flightQueryService = flightQueryService;
        this.passengerReservationRepository = passengerReservationRepository;
    }

    public BigDecimal calculatePrice(FlightEntity flight, SeatClass seatClass) {
        long occupied = passengerReservationRepository.countByFlightAndSeatClassAndStatusNot(flight, seatClass, PassengerReservationStatus.CANCELED);
        return flight.calculatePrice(seatClass, occupied);
    }

    public FlightPriceQuoteDto getQuote(Long flightId) {
        FlightEntity flight = flightQueryService.getFlightEntity(flightId);
        return new FlightPriceQuoteDto(
                flightId,
                calculatePrice(flight, SeatClass.ECONOMY),
                calculatePrice(flight, SeatClass.FIRST_CLASS)
        );
    }
}
