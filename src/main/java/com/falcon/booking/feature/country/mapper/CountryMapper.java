package com.falcon.booking.feature.country.mapper;
import com.falcon.booking.persistence.entity.CountryEntity;
import com.falcon.booking.feature.country.dto.CountryDto;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CountryMapper {

    CountryDto toDto(CountryEntity countryEntity);
    List<CountryDto> toDto(List<CountryEntity> countryEntities);
}
