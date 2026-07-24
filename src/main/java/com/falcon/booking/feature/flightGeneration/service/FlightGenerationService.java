package com.falcon.booking.feature.flightGeneration.service;

import com.falcon.booking.feature.flightGeneration.dto.ResponseFlightsGenerationDto;
import com.falcon.booking.feature.flightGeneration.exception.FlightGenerationAlreadyRunningException;
import com.falcon.booking.feature.flightGeneration.exception.FlightGenerationNotFoundException;
import com.falcon.booking.feature.flightGeneration.mapper.FlightGenerationMapper;
import com.falcon.booking.feature.route.exception.RouteAirplaneTypeIsNotActiveException;
import com.falcon.booking.feature.route.exception.RouteNotActiveException;
import com.falcon.booking.feature.route.service.RouteQueryService;
import com.falcon.booking.persistence.entity.AirplaneTypeEntity;
import com.falcon.booking.persistence.entity.FlightGenerationEntity;
import com.falcon.booking.persistence.entity.RouteEntity;
import com.falcon.booking.persistence.repository.FlightGenerationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;

@Service
public class FlightGenerationService {

    private final FlightGenerationRepository flightGenerationRepository;
    private final FlightGenerationMapper flightGenerationMapper;
    private final AsyncFlightGenerationService asyncFlightGenerationService;
    private final RouteQueryService routeQueryService;

    @Autowired
    public FlightGenerationService(FlightGenerationRepository flightGenerationRepository, FlightGenerationMapper flightGenerationMapper, AsyncFlightGenerationService asyncFlightGenerationService, RouteQueryService routeQueryService) {
        this.flightGenerationRepository = flightGenerationRepository;
        this.flightGenerationMapper = flightGenerationMapper;
        this.asyncFlightGenerationService = asyncFlightGenerationService;
        this.routeQueryService = routeQueryService;
    }

    public ResponseFlightsGenerationDto getFlightGeneration(Long id){
        FlightGenerationEntity entity = flightGenerationRepository.findById(id)
                .orElseThrow(()-> new FlightGenerationNotFoundException(id));

        return flightGenerationMapper.toDto(entity);
    }

    public Page<ResponseFlightsGenerationDto> getAllFlightGenerations(int page, int size){
        Pageable pageable = PageRequest.of(page, size, Sort.by("startedAt").descending());
        return flightGenerationRepository.findAll(pageable).map(flightGenerationMapper::toDto);
    }

    public ResponseFlightsGenerationDto startGlobalFlightGeneration() {
        try {
            FlightGenerationEntity generation = FlightGenerationEntity.startGlobalGeneration();
            FlightGenerationEntity generationSaved = flightGenerationRepository.save(generation);
            asyncFlightGenerationService.executeGeneration(generationSaved.getId());
            return flightGenerationMapper.toDto(generationSaved);

        } catch (DataIntegrityViolationException e) {
            String constraint = extractConstraintName(e).orElse("");
            if ("idx_flight_generation_only_one_running".equals(constraint))
                throw new FlightGenerationAlreadyRunningException();
            else throw e;
        }
    }

    public ResponseFlightsGenerationDto startRouteFlightGeneration(String flightNumber) {

        RouteEntity routeEntity = routeQueryService.getRouteEntity(flightNumber);

        checkRouteIsActive(routeEntity);
        checkAirplaneTypeIsActive(routeEntity.getDefaultAirplaneType());

        try {
            FlightGenerationEntity generation = FlightGenerationEntity.startRouteGeneration(routeEntity.getId());
            FlightGenerationEntity generationSaved = flightGenerationRepository.save(generation);
            asyncFlightGenerationService.executeGeneration(generationSaved.getId());
            return flightGenerationMapper.toDto(generationSaved);

        } catch (DataIntegrityViolationException e) {
            String constraint = extractConstraintName(e).orElse("");
            if ("idx_flight_generation_only_one_running".equals(constraint))
                throw new FlightGenerationAlreadyRunningException();
            else throw e;
        }
    }

    private void checkRouteIsActive(RouteEntity route) {
        if (!route.isActive())
            throw new RouteNotActiveException(route.getFlightNumber());
    }

    private void checkAirplaneTypeIsActive(AirplaneTypeEntity airplaneType) {
        if (!airplaneType.isActive())
            throw new RouteAirplaneTypeIsNotActiveException(airplaneType.getId());
    }

    public void startDailyFlightGeneration(LocalDate targetDate) {

        try {
            FlightGenerationEntity generation = FlightGenerationEntity.startDailyGeneration(targetDate);
            FlightGenerationEntity generationSaved = flightGenerationRepository.save(generation);
            asyncFlightGenerationService.executeGeneration(generationSaved.getId());

        } catch (DataIntegrityViolationException e) {
            String constraint = extractConstraintName(e).orElse("");
            if ("idx_flight_generation_only_one_running".equals(constraint))
                throw new FlightGenerationAlreadyRunningException();
            else throw e;
        }
    }

    private Optional<String> extractConstraintName(Throwable e) {
        Throwable cause = e;

        while (cause != null) {
            if (cause instanceof org.hibernate.exception.ConstraintViolationException ex) {
                return Optional.ofNullable(ex.getConstraintName());
            }
            cause = cause.getCause();
        }
        return Optional.empty();
    }

}
