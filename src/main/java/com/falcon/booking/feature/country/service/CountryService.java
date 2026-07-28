package com.falcon.booking.feature.country.service;

import com.falcon.booking.common.utils.StringNormalizer;
import com.falcon.booking.feature.country.dto.CountryDto;
import com.falcon.booking.feature.country.dto.CreateCountryDto;
import com.falcon.booking.feature.country.exception.CountryAlreadyExistsException;
import com.falcon.booking.feature.country.exception.CountryNotFoundException;
import com.falcon.booking.feature.country.mapper.CountryMapper;
import com.falcon.booking.persistence.entity.CountryEntity;
import com.falcon.booking.persistence.repository.CountryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional
    public CountryDto createCountry(CreateCountryDto dto) {
        String normalizedIsoCode = StringNormalizer.normalize(dto.isoCode());

        if (countryRepository.findByIsoCode(normalizedIsoCode).isPresent()) {
            throw new CountryAlreadyExistsException("ISO code", dto.isoCode());
        }

        String trimmedName = dto.name().trim();
        if (countryRepository.existsByNameIgnoreCase(trimmedName)) {
            throw new CountryAlreadyExistsException("name", trimmedName);
        }

        CountryEntity country = countryMapper.toEntity(dto);
        country.setIsoCode(normalizedIsoCode);
        country.setName(trimmedName);

        CountryEntity saved = countryRepository.save(country);
        return countryMapper.toDto(saved);
    }

}
