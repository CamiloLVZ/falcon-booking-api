package com.falcon.booking.feature.flight.mapper;

import com.falcon.booking.feature.airplaneType.dto.AirplaneTypeInFlightDto;
import com.falcon.booking.feature.airplaneType.mapper.AirplaneTypeMapper;
import com.falcon.booking.feature.flight.dto.ResponseFlightDto;
import com.falcon.booking.persistence.entity.FlightEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Component
public class FlightMapper {

    private final AirplaneTypeMapper airplaneTypeMapper;

    @Autowired
    public FlightMapper(AirplaneTypeMapper airplaneTypeMapper) {
        this.airplaneTypeMapper = airplaneTypeMapper;
    }

    public ResponseFlightDto toDto(FlightEntity flightEntity) {

        AirplaneTypeInFlightDto airplaneTypeDto = airplaneTypeMapper.toInFlightDto(flightEntity.getAirplaneType());
        ZoneId timezone = ZoneId.of(flightEntity.getRoute().getAirportOrigin().getTimezone());
        LocalDateTime localDepartureDateTime = flightEntity.getDepartureDateTime().atZoneSameInstant(timezone).toLocalDateTime();


        return new ResponseFlightDto(flightEntity.getId(), flightEntity.getRoute().getFlightNumber(),
                flightEntity.getRoute().getAirportOrigin().getIataCode(),
                flightEntity.getRoute().getAirportDestination().getIataCode(),flightEntity.getDepartureDateTime(),
                localDepartureDateTime, flightEntity.getRoute().getDurationMinutes(), airplaneTypeDto, flightEntity.getStatus(),
                flightEntity.getBasePriceEconomy(), flightEntity.getBasePriceFirstClass());
    }
    public List<ResponseFlightDto> toDto (List<FlightEntity> entities){
        return entities.stream()
                .map(this::toDto)
                .toList();
    }
}
