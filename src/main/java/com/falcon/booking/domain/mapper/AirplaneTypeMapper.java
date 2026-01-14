package com.falcon.booking.domain.mapper;

import com.falcon.booking.persistence.entity.AirplaneTypeEntity;
import com.falcon.booking.web.dto.AirplaneTypeDto.AirplaneTypeResponseDto;
import com.falcon.booking.web.dto.AirplaneTypeDto.CreateAirplaneTypeDto;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AirplaneTypeMapper {

    AirplaneTypeResponseDto toResponseDto(AirplaneTypeEntity airplaneTypeEntity);
    List<AirplaneTypeResponseDto> toResponseDto(List<AirplaneTypeEntity> airplaneTypeEntities);
    AirplaneTypeEntity toEntity(CreateAirplaneTypeDto createAirplaneTypeDto);

}
