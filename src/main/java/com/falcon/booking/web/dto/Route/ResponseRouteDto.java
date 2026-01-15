package com.falcon.booking.web.dto.Route;

import com.falcon.booking.domain.valueobject.RouteStatus;
import com.falcon.booking.web.dto.AirplaneTypeDto.AirplaneTypeResponseDto;
import com.falcon.booking.web.dto.AirportDto;

public record ResponseRouteDto(
        String flightNumber,
        AirportDto airportOrigin,
        AirportDto airportDestination,
        AirplaneTypeResponseDto defaultAirplaneType,
        int lengthMinutes,
        RouteStatus status
) {
}
