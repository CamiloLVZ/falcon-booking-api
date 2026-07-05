package com.falcon.booking.feature.country.service;

import com.falcon.booking.common.utils.StringNormalizer;
import com.falcon.booking.feature.country.dto.CountryDto;
import com.falcon.booking.feature.country.exception.CountryNotFoundException;
import com.falcon.booking.feature.country.mapper.CountryMapper;
import com.falcon.booking.persistence.entity.CountryEntity;
import com.falcon.booking.persistence.repository.CountryRepository;
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
                orElseThrow( ()->new CountryNotFoundException(isoCode) );
    }

    public CountryDto getCountryByIsoCode(String isoCode) {

        CountryEntity countryEntity = getCountryEntityByIsoCode(isoCode);
        return countryMapper.toDto(countryEntity);
    }

    public List<CountryDto> getAllCountries() {
        List<CountryEntity> listCountryEntity = countryRepository.findAllByOrderByNameAsc();
        return countryMapper.toDto(listCountryEntity);
    }

}
