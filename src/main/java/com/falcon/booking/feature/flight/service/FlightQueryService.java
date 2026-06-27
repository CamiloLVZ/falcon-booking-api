package com.falcon.booking.feature.flight.service;

import com.falcon.booking.common.enums.FlightStatus;
import com.falcon.booking.common.utils.DateTimeUtils;
import com.falcon.booking.feature.airport.service.AirportService;
import com.falcon.booking.feature.flight.dto.ResponseFlightDto;
import com.falcon.booking.feature.flight.exception.FlightNotFoundException;
import com.falcon.booking.feature.flight.mapper.FlightMapper;
import com.falcon.booking.feature.route.exception.RouteNotActiveException;
import com.falcon.booking.feature.route.service.RouteQueryService;
import com.falcon.booking.persistence.entity.AirportEntity;
import com.falcon.booking.persistence.entity.FlightEntity;
import com.falcon.booking.persistence.entity.RouteEntity;
import com.falcon.booking.persistence.repository.FlightRepository;
import com.falcon.booking.persistence.specification.FlightSpecifications;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Map;

@Service
public class FlightQueryService {

    private final FlightRepository flightRepository;
    private final RouteQueryService routeQueryService;
    private final AirportService airportService;
    private final FlightMapper flightMapper;

    public FlightQueryService(FlightRepository flightRepository, RouteQueryService routeQueryService, AirportService airportService, FlightMapper flightMapper) {
        this.flightRepository = flightRepository;
        this.routeQueryService = routeQueryService;
        this.airportService = airportService;
        this.flightMapper = flightMapper;
    }

    public FlightEntity getFlightEntity(Long id) {
        return flightRepository.findById(id)
                .orElseThrow(() -> new FlightNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public ResponseFlightDto getFlightById(Long id) {
        return flightMapper.toDto(getFlightEntity(id));
    }

    @Transactional(readOnly = true)
    public Page<ResponseFlightDto> getAllFlights(String flightNumber, FlightStatus flightStatus, LocalDate dateFrom, LocalDate dateTo, int page, int size) {
        RouteEntity route = routeQueryService.getRouteEntity(flightNumber);
        ZoneId timezone = ZoneId.of(route.getAirportOrigin().getTimezone());
        OffsetDateTime offsetDateTimeFrom = DateTimeUtils.toOffsetDateTime(dateFrom, timezone);
        OffsetDateTime offsetDateTimeTo = DateTimeUtils.toOffsetDateTime(dateTo, timezone);

        Specification<FlightEntity> spec = Specification.allOf();
        spec = spec.and(FlightSpecifications.hasRoute(route));
        spec = spec.and(FlightSpecifications.hasStatus(flightStatus));
        spec = spec.and(FlightSpecifications.hasDateStart(offsetDateTimeFrom));
        spec = spec.and(FlightSpecifications.hasDateEnd(offsetDateTimeTo));

        Pageable pageable = PageRequest.of(page, size, Sort.by("departureDateTime").ascending());
        return flightRepository.findAll(spec, pageable).map(flightMapper::toDto);
    }

    public Page<ResponseFlightDto> getAllFlightsByOriginDestinationAndDate(String originIataCode, String destinationIataCode, LocalDate date, FlightStatus status, int page, int size) {
        AirportEntity airportOrigin = airportService.getAirportEntityByIataCode(originIataCode);
        if (status == null) status = FlightStatus.SCHEDULED;

        Map<String, OffsetDateTime> dayRange = DateTimeUtils.getDayRange(date, ZoneId.of(airportOrigin.getTimezone()));
        OffsetDateTime startDateTime = dayRange.get("start");
        OffsetDateTime endDateTime = dayRange.get("end");

        Pageable pageable = PageRequest.of(page, size, Sort.by("departureDateTime").ascending());
        return flightRepository.findFlightsByAirportsAndDate(originIataCode, destinationIataCode, startDateTime, endDateTime, status, pageable)
                .map(flightMapper::toDto);
    }

    @Transactional(readOnly = true)
    public Page<ResponseFlightDto> getAllFlightsByRouteAndDate(String flightNumber, LocalDate date, int page, int size) {
        RouteEntity routeEntity = routeQueryService.getRouteEntity(flightNumber);
        if (!routeEntity.isActive()) throw new RouteNotActiveException(routeEntity.getFlightNumber());

        Map<String, OffsetDateTime> dayRange = DateTimeUtils.getDayRange(date, ZoneId.of(routeEntity.getAirportOrigin().getTimezone()));
        OffsetDateTime startDateTime = dayRange.get("start");
        OffsetDateTime endDateTime = dayRange.get("end");

        Pageable pageable = PageRequest.of(page, size, Sort.by("departureDateTime").ascending());
        return flightRepository.findAllByRouteAndDepartureDateTimeBetween(routeEntity, startDateTime, endDateTime, pageable)
                .map(flightMapper::toDto);
    }
}
