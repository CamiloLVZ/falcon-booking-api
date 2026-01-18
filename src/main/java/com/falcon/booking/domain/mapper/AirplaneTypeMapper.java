package com.falcon.booking.domain.mapper;

import com.falcon.booking.persistence.entity.AirplaneTypeEntity;
import com.falcon.booking.web.dto.airplaneType.AirplaneTypeInFlightDto;
import com.falcon.booking.web.dto.airplaneType.ResponseAirplaneTypeDto;
import com.falcon.booking.web.dto.airplaneType.CreateAirplaneTypeDto;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AirplaneTypeMapper {

    ResponseAirplaneTypeDto toResponseDto(AirplaneTypeEntity airplaneTypeEntity);
    List<ResponseAirplaneTypeDto> toResponseDto(List<AirplaneTypeEntity> airplaneTypeEntities);
    AirplaneTypeEntity toEntity(CreateAirplaneTypeDto createAirplaneTypeDto);
    AirplaneTypeInFlightDto toInFlightDto(AirplaneTypeEntity airplaneTypeEntity);
}
