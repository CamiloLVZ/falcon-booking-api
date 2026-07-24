package com.falcon.booking.feature.airplaneType.mapper;

import com.falcon.booking.feature.airplaneType.dto.AirplaneTypeInFlightDto;
import com.falcon.booking.feature.airplaneType.dto.CreateAirplaneTypeDto;
import com.falcon.booking.feature.airplaneType.dto.ResponseAirplaneTypeDto;
import com.falcon.booking.persistence.entity.AirplaneTypeEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AirplaneTypeMapper {

    ResponseAirplaneTypeDto toResponseDto(AirplaneTypeEntity airplaneTypeEntity);
    List<ResponseAirplaneTypeDto> toResponseDto(List<AirplaneTypeEntity> airplaneTypeEntities);
    AirplaneTypeInFlightDto toInFlightDto(AirplaneTypeEntity airplaneTypeEntity);

    /**
     * Maps only producer and model from the DTO. Seat fields (economySeats, firstClassSeats,
     * seatColumns) have no public setters on the entity — they are assigned after creation
     * via {@code entity.configureSeats()} in the service layer.
     */
    @Mapping(target = "economySeats", ignore = true)
    @Mapping(target = "firstClassSeats", ignore = true)
    @Mapping(target = "seatColumns", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    AirplaneTypeEntity toEntity(CreateAirplaneTypeDto createAirplaneTypeDto);
}
