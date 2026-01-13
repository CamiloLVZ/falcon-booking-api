package com.falcon.booking.domain.mapper;

import com.falcon.booking.persistence.entity.AirportEntity;
import com.falcon.booking.web.dto.AirportDto;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AirportMapper {
    AirportDto toDto(AirportEntity airportEntity);
    List<AirportDto> toDto(List<AirportEntity> airportEntities);
}
