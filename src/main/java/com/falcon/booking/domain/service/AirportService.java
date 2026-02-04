package com.falcon.booking.domain.service;

import com.falcon.booking.domain.common.utils.StringNormalizer;
import com.falcon.booking.domain.exception.AirportDoesNotExistException;
import com.falcon.booking.domain.mapper.AirportMapper;
import com.falcon.booking.persistence.entity.AirportEntity;
import com.falcon.booking.persistence.repository.AirportRepository;
import com.falcon.booking.web.dto.AirportDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AirportService {

    private final AirportRepository airportRepository;
    private final AirportMapper airportMapper;

    @Autowired
    public AirportService(AirportRepository airportRepository, AirportMapper airportMapper) {
        this.airportRepository = airportRepository;
        this.airportMapper = airportMapper;
    }

    public AirportEntity getAirportEntityByIataCode(String iataCode) {
        String normalizedIataCode = StringNormalizer.normalize(iataCode);

        return airportRepository.findByIataCode(normalizedIataCode).orElseThrow(
                () -> new AirportDoesNotExistException(iataCode)
        );
    }

    @Transactional(readOnly = true)
    public AirportDto getAirportByIataCode(String iataCode) {
        return airportMapper.toDto(getAirportEntityByIataCode(iataCode));
    }

    @Transactional(readOnly = true)
    public List<AirportDto> getAllAirports() {
        List<AirportEntity> airportEntities = airportRepository.findAll();
        return airportMapper.toDto(airportEntities);
    }
    @Transactional(readOnly = true)
    public List<AirportDto> getAirportsByCountryIsoCode(String isoCode) {
        String isoCodeNormalized= StringNormalizer.normalize(isoCode);
        List<AirportEntity> airportEntities = airportRepository.findAllByCountryIsoCode(isoCodeNormalized);

        return airportMapper.toDto(airportEntities);
    }

}
