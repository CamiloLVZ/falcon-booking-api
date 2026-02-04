package com.falcon.booking.domain.service;

import com.falcon.booking.domain.common.utils.StringNormalizer;
import com.falcon.booking.domain.exception.CountryDoesNotExistException;
import com.falcon.booking.domain.mapper.CountryMapper;
import com.falcon.booking.persistence.entity.CountryEntity;
import com.falcon.booking.persistence.repository.CountryRepository;
import com.falcon.booking.web.dto.CountryDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CountryService {

    private final CountryRepository countryRepository;
    private final CountryMapper countryMapper;

    @Autowired
    public CountryService(CountryRepository countryRepository, CountryMapper countryMapper) {
        this.countryRepository = countryRepository;
        this.countryMapper = countryMapper;
    }

    public CountryEntity getCountryEntityByIsoCode(String isoCode) {
        String isoCodeNormalized= StringNormalizer.normalize(isoCode);
        return countryRepository.findByIsoCode(isoCodeNormalized).
                orElseThrow( ()->new CountryDoesNotExistException(isoCode) );
    }

    public CountryDto getCountryByIsoCode(String isoCode) {

        CountryEntity countryEntity = getCountryEntityByIsoCode(isoCode);
        return countryMapper.toDto(countryEntity);
    }

    public List<CountryDto> getAllCountries() {
        List<CountryEntity> listCountryEntity = countryRepository.findAll();
        return countryMapper.toDto(listCountryEntity);
    }

}
