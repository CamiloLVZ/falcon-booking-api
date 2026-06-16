package com.falcon.booking.feature.airport.mapper;

import com.falcon.booking.persistence.entity.AirportEntity;
import com.falcon.booking.feature.airport.dto.AirportDto;
import com.falcon.booking.feature.airport.dto.AirportSearchOptionDto;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AirportMapper {
    AirportDto toDto(AirportEntity airportEntity);
    List<AirportDto> toDto(List<AirportEntity> airportEntities);
    AirportSearchOptionDto toSearchOptionDto(AirportEntity airportEntity);
    List<AirportSearchOptionDto> toSearchOptionDto(List<AirportEntity> airportEntities);
}
