package com.falcon.booking.feature.airport.service;

import com.falcon.booking.common.utils.StringNormalizer;
import com.falcon.booking.feature.airport.exception.AirportNotFoundException;
import com.falcon.booking.feature.airport.mapper.AirportMapper;
import com.falcon.booking.feature.country.service.CountryService;
import com.falcon.booking.persistence.entity.AirportEntity;
import com.falcon.booking.persistence.entity.CountryEntity;
import com.falcon.booking.persistence.repository.AirportRepository;
import com.falcon.booking.feature.airport.dto.AirportDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AirportService {

    private final AirportRepository airportRepository;
    private final AirportMapper airportMapper;
    private final CountryService countryService;

    @Autowired
    public AirportService(AirportRepository airportRepository, AirportMapper airportMapper, CountryService countryService) {
        this.airportRepository = airportRepository;
        this.airportMapper = airportMapper;
        this.countryService = countryService;
    }

    public AirportEntity getAirportEntityByIataCode(String iataCode) {
        String normalizedIataCode = StringNormalizer.normalize(iataCode);

        return airportRepository.findByIataCode(normalizedIataCode).orElseThrow(
                () -> new AirportNotFoundException(iataCode)
        );
    }

    @Transactional(readOnly = true)
    public AirportDto getAirportByIataCode(String iataCode) {
        return airportMapper.toDto(getAirportEntityByIataCode(iataCode));
    }

    @Transactional(readOnly = true)
    public Page<AirportDto> getAllAirports(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("city").ascending());
        Page<AirportEntity> airportEntities = airportRepository.findAll(pageable);
        return airportEntities.map(airportMapper::toDto);
    }
    @Transactional(readOnly = true)
    public Page<AirportDto> getAirportsByCountryIsoCode(String isoCode, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("city").ascending());
        CountryEntity country = countryService.getCountryEntityByIsoCode(isoCode);
        Page<AirportEntity> airportEntities = airportRepository.findAllByCountry(country,  pageable);
        return airportEntities.map(airportMapper::toDto);
    }

}
