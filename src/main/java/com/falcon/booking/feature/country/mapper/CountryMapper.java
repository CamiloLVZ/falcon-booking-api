package com.falcon.booking.feature.country.mapper;

import com.falcon.booking.feature.country.dto.CountryDto;
import com.falcon.booking.feature.country.dto.CreateCountryDto;
import com.falcon.booking.persistence.entity.CountryEntity;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CountryMapper {

    CountryDto toDto(CountryEntity countryEntity);
    List<CountryDto> toDto(List<CountryEntity> countryEntities);
    CountryEntity toEntity(CreateCountryDto createCountryDto);
}
