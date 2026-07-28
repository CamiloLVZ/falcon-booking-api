package com.falcon.booking.feature.reservation.mapper;

import com.falcon.booking.feature.passenger.mapper.PassengerMapper;
import com.falcon.booking.feature.reservation.dto.ResponsePassengerReservationDto;
import com.falcon.booking.persistence.entity.PassengerReservationEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PassengerReservationMapper {

    private final PassengerMapper passengerMapper;

    public PassengerReservationMapper(PassengerMapper passengerMapper) {
        this.passengerMapper = passengerMapper;
    }


    public ResponsePassengerReservationDto toResponseDto(PassengerReservationEntity entity) {
        String seatLabel = entity.getSeatNumber() != null
                ? entity.getFlight().getAirplaneType().getSeatLabel(entity.getSeatNumber())
                : null;
        return new ResponsePassengerReservationDto(entity.getId(), passengerMapper.toResponseDto(entity.getPassenger()), entity.getSeatNumber(), seatLabel, entity.getSeatClass(), entity.getStatus());
    }

    public List<ResponsePassengerReservationDto> toResponseDto(List<PassengerReservationEntity> entities) {
        return entities.stream()
                .map(this::toResponseDto)
                .toList();
    }
}
