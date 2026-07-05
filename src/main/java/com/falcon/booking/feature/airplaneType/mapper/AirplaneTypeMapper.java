package com.falcon.booking.feature.airplaneType.mapper;

import com.falcon.booking.feature.airplaneType.dto.AirplaneTypeInFlightDto;
import com.falcon.booking.feature.airplaneType.dto.CreateAirplaneTypeDto;
import com.falcon.booking.feature.airplaneType.dto.ResponseAirplaneTypeDto;
import com.falcon.booking.persistence.entity.AirplaneTypeEntity;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AirplaneTypeMapper {

    ResponseAirplaneTypeDto toResponseDto(AirplaneTypeEntity airplaneTypeEntity);
    List<ResponseAirplaneTypeDto> toResponseDto(List<AirplaneTypeEntity> airplaneTypeEntities);
    AirplaneTypeEntity toEntity(CreateAirplaneTypeDto createAirplaneTypeDto);
    AirplaneTypeInFlightDto toInFlightDto(AirplaneTypeEntity airplaneTypeEntity);
}
