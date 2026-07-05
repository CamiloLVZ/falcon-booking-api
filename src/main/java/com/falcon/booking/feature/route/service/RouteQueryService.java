package com.falcon.booking.feature.route.service;

import com.falcon.booking.common.enums.RouteStatus;
import com.falcon.booking.common.utils.StringNormalizer;
import com.falcon.booking.feature.airport.dto.AirportSearchOptionDto;
import com.falcon.booking.feature.airport.mapper.AirportMapper;
import com.falcon.booking.feature.route.dto.ResponseRouteDto;
import com.falcon.booking.feature.route.exception.RouteNotFoundException;
import com.falcon.booking.feature.route.mapper.RouteMapper;
import com.falcon.booking.persistence.entity.RouteEntity;
import com.falcon.booking.persistence.repository.RouteRepository;
import com.falcon.booking.persistence.specification.RouteSpecifications;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RouteQueryService {

    private final RouteRepository routeRepository;
    private final RouteMapper routeMapper;
    private final AirportMapper airportMapper;

    public RouteQueryService(RouteRepository routeRepository, RouteMapper routeMapper, AirportMapper airportMapper) {
        this.routeRepository = routeRepository;
        this.routeMapper = routeMapper;
        this.airportMapper = airportMapper;
    }

    public RouteEntity getRouteEntity(String flightNumber) {
        String normalized = StringNormalizer.normalize(flightNumber);
        return routeRepository.findByFlightNumber(normalized)
                .orElseThrow(() -> new RouteNotFoundException(normalized));
    }

    public boolean existsByFlightNumber(String flightNumber) {
        String normalized = StringNormalizer.normalize(flightNumber);
        return routeRepository.existsByFlightNumber(normalized);
    }

    @Transactional(readOnly = true)
    public List<RouteEntity> getAllRoutesByStatus(RouteStatus status) {
        return routeRepository.findAllByStatus(status);
    }

    @Transactional(readOnly = true)
    public ResponseRouteDto getRouteByFlightNumber(String flightNumber) {
        return routeMapper.toResponseDto(getRouteEntity(flightNumber));
    }

    public List<AirportSearchOptionDto> getOriginAirports() {
        return airportMapper.toSearchOptionDto(routeRepository.findDistinctOrigins());
    }

    public List<AirportSearchOptionDto> getDestinationAirports(String originIataCode) {
        return airportMapper.toSearchOptionDto(routeRepository.findDestinationsByOrigin(StringNormalizer.normalize(originIataCode)));
    }

    @Transactional(readOnly = true)
    public Page<ResponseRouteDto> getAllRoutes(String airportOriginIataCode, String airportDestinationIataCode, RouteStatus status, int page, int size) {
        String normalizedOrigin = StringNormalizer.normalize(airportOriginIataCode);
        String normalizedDestination = StringNormalizer.normalize(airportDestinationIataCode);

        Specification<RouteEntity> specification = Specification.allOf();
        specification = specification.and(RouteSpecifications.hasOriginIataCode(normalizedOrigin));
        specification = specification.and(RouteSpecifications.hasDestinationIataCode(normalizedDestination));
        specification = specification.and(RouteSpecifications.hasStatus(status));

        Pageable pageable = PageRequest.of(page, size, Sort.by("flightNumber").ascending());
        return routeRepository.findAll(specification, pageable).map(routeMapper::toResponseDto);
    }
}
