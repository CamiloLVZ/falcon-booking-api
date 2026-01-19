package com.falcon.booking.web.dto.flight;

import com.falcon.booking.domain.valueobject.FlightStatus;
import com.falcon.booking.web.dto.airplaneType.AirplaneTypeInFlightDto;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

public record ResponseFlightDto(Long id,
                                String flightNumber,
                                String origin,
                                String destination,
                                OffsetDateTime departureDateTime,
                                LocalDateTime localDepartureDateTime,
                                AirplaneTypeInFlightDto airplaneType,
                                FlightStatus status) {
}
