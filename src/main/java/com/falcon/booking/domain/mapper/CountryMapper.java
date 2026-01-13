package com.falcon.booking.domain.mapper;
import com.falcon.booking.persistence.entity.CountryEntity;
import com.falcon.booking.web.dto.CountryDto;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CountryMapper {

    CountryDto toDto(CountryEntity countryEntity);
    List<CountryDto> toDto(List<CountryEntity> countryEntities);

}
