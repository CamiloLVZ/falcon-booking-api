package com.falcon.booking.feature.reservation.mapper;

import com.falcon.booking.feature.passenger.mapper.PassengerMapper;
import com.falcon.booking.persistence.entity.PassengerReservationEntity;
import com.falcon.booking.feature.reservation.dto.ResponsePassengerReservationDto;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class PassengerReservationMapper {

    private final PassengerMapper passengerMapper;

    public PassengerReservationMapper(PassengerMapper passengerMapper) {
        this.passengerMapper = passengerMapper;
    }


    public ResponsePassengerReservationDto toResponseDto(PassengerReservationEntity entity) {
        return new ResponsePassengerReservationDto(passengerMapper.toResponseDto(entity.getPassenger()), entity.getSeatNumber(), entity.getStatus());
    }

    public List<ResponsePassengerReservationDto> toResponseDto(List<PassengerReservationEntity> entities) {
        List<ResponsePassengerReservationDto> dtos = new ArrayList<>();
        for (PassengerReservationEntity entity : entities) {
            dtos.add(toResponseDto(entity));
        }
        return dtos;
    }


}
