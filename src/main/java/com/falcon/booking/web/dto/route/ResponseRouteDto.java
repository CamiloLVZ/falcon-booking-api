package com.falcon.booking.web.dto.route;

import com.falcon.booking.domain.valueobject.RouteStatus;
import com.falcon.booking.web.dto.airplaneType.ResponseAirplaneTypeDto;
import com.falcon.booking.web.dto.AirportDto;

public record ResponseRouteDto(
        String flightNumber,
        AirportDto airportOrigin,
        AirportDto airportDestination,
        ResponseAirplaneTypeDto defaultAirplaneType,
        int lengthMinutes,
        RouteStatus status
) {
}
