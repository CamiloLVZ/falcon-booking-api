package com.falcon.booking.feature.flight.service;

import com.falcon.booking.common.enums.FlightStatus;
import com.falcon.booking.feature.boarding.service.BoardingService;
import com.falcon.booking.persistence.entity.FlightEntity;
import com.falcon.booking.persistence.repository.FlightRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Service
public class FlightStatusService {

    @Value("${app.flight.check-in.hours-before-to-start}")
    int checkInHoursBeforeToStart;
    @Value("${app.flight.check-in.hours-before-to-close}")
    int checkInHoursBeforeToClose;
    @Value("${app.flight.boarding.minutes-before-to-start}")
    int boardingMinutesBeforeToStart;
    @Value("${app.flight.boarding.minutes-before-to-close}")
    int boardingMinutesBeforeToClose;

    private final FlightRepository flightRepository;
    private final BoardingService boardingService;

    public FlightStatusService(FlightRepository flightRepository, BoardingService boardingService) {
        this.flightRepository = flightRepository;
        this.boardingService = boardingService;
    }

    public boolean updateFlightStatus(FlightEntity flight, OffsetDateTime now) {
        OffsetDateTime departureDateTime = flight.getDepartureDateTime();
        OffsetDateTime checkInStart = departureDateTime.minusHours(checkInHoursBeforeToStart);
        OffsetDateTime checkInEnd = departureDateTime.minusHours(checkInHoursBeforeToClose);
        OffsetDateTime boardingStart = departureDateTime.minusMinutes(boardingMinutesBeforeToStart);
        OffsetDateTime boardingEnd = departureDateTime.minusMinutes(boardingMinutesBeforeToClose);

        boolean isInCheckInRange = !now.isBefore(checkInStart) && !now.isAfter(checkInEnd);
        boolean isInBoardingRange = !now.isBefore(boardingStart) && !now.isAfter(boardingEnd);
        boolean isGateClosedRange = now.isAfter(boardingEnd) && !now.isAfter(departureDateTime);

        flight.correctStatusByTime(now);

        if (now.isAfter(departureDateTime)) {
            boolean wasCompleted = flight.isCompleted();
            return !wasCompleted;
        }
        
        if (isGateClosedRange && !flight.isGateClosed()) {
            flight.markAsGateClosed();
            boardingService.expireUnusedPassesForFlight(flight.getId());
            return true;
        }

        if (isInBoardingRange && !flight.isInBoarding()) {
            flight.startBoarding();
            return true;
        }
        
        if (isInCheckInRange && !flight.isCheckInAvailable()) {
            flight.startCheckIn();
            return true;
        }
        
        return false;
    }

    @Transactional
    public int updateFlightsStatus() {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        List<FlightEntity> flightsToUpdate = flightRepository.findAllByStatusNotAndStatusNot(FlightStatus.CANCELED, FlightStatus.COMPLETED);

        int updatesCounter = 0;
        for (FlightEntity flight : flightsToUpdate) {
            boolean updated = updateFlightStatus(flight, now);
            if (updated) updatesCounter++;
        }
        return updatesCounter;
    }
}
