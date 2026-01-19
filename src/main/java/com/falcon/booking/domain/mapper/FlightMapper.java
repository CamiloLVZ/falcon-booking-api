package com.falcon.booking.domain.mapper;

import com.falcon.booking.persistence.entity.FlightEntity;
import com.falcon.booking.web.dto.airplaneType.AirplaneTypeInFlightDto;
import com.falcon.booking.web.dto.flight.ResponseFlightDto;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
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
                localDepartureDateTime, airplaneTypeDto, flightEntity.getStatus());
    }
    public List<ResponseFlightDto> toDto (List<FlightEntity> entities){
        List<ResponseFlightDto> dtoList = new ArrayList<>();
        for (FlightEntity flightEntity : entities) {
            dtoList.add(toDto(flightEntity));
        }
        return dtoList;
    }
}
