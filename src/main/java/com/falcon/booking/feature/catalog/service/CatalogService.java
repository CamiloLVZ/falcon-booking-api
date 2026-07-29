package com.falcon.booking.feature.catalog.service;

import com.falcon.booking.common.enums.AirplaneTypeStatus;
import com.falcon.booking.feature.airplaneType.dto.AirplaneTypeOptionDto;
import com.falcon.booking.feature.airplaneType.mapper.AirplaneTypeMapper;
import com.falcon.booking.feature.airport.dto.AirportSearchOptionDto;
import com.falcon.booking.feature.airport.mapper.AirportMapper;
import com.falcon.booking.feature.catalog.dto.CatalogDropdownDto;
import com.falcon.booking.feature.country.dto.CountryDto;
import com.falcon.booking.feature.country.mapper.CountryMapper;
import com.falcon.booking.feature.route.service.RouteQueryService;
import com.falcon.booking.persistence.entity.AirplaneTypeEntity;
import com.falcon.booking.persistence.repository.AirplaneTypeRepository;
import com.falcon.booking.persistence.repository.AirportRepository;
import com.falcon.booking.persistence.repository.CountryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CatalogService {

    private final AirportRepository airportRepository;
    private final AirplaneTypeRepository airplaneTypeRepository;
    private final CountryRepository countryRepository;
    private final AirportMapper airportMapper;
    private final AirplaneTypeMapper airplaneTypeMapper;
    private final CountryMapper countryMapper;
    private final RouteQueryService routeQueryService;

    public CatalogService(AirportRepository airportRepository, AirplaneTypeRepository airplaneTypeRepository,
                          CountryRepository countryRepository, AirportMapper airportMapper,
                          AirplaneTypeMapper airplaneTypeMapper, CountryMapper countryMapper,
                          RouteQueryService routeQueryService) {
        this.airportRepository = airportRepository;
        this.airplaneTypeRepository = airplaneTypeRepository;
        this.countryRepository = countryRepository;
        this.airportMapper = airportMapper;
        this.airplaneTypeMapper = airplaneTypeMapper;
        this.countryMapper = countryMapper;
        this.routeQueryService = routeQueryService;
    }

    @Transactional(readOnly = true)
    public CatalogDropdownDto getDropdownOptions(boolean isAdmin) {
        List<AirportSearchOptionDto> airports = airportMapper.toSearchOptionDto(airportRepository.findAll());

        List<AirplaneTypeOptionDto> airplaneTypes;
        if (isAdmin) {
            airplaneTypes = airplaneTypeMapper.toOptionDto(airplaneTypeRepository.findAll());
        } else {
            List<AirplaneTypeEntity> activeEntities = airplaneTypeRepository.findByStatus(AirplaneTypeStatus.ACTIVE);
            airplaneTypes = airplaneTypeMapper.toOptionDto(activeEntities);
        }

        List<CountryDto> countries = countryMapper.toDto(countryRepository.findAllByOrderByNameAsc());

        return new CatalogDropdownDto(airports, airplaneTypes, countries);
    }

    @Transactional(readOnly = true)
    public List<AirportSearchOptionDto> getOriginAirports() {
        return routeQueryService.getOriginAirports();
    }

    @Transactional(readOnly = true)
    public List<AirportSearchOptionDto> getDestinationAirports(String originIataCode) {
        return routeQueryService.getDestinationAirports(originIataCode);
    }
}