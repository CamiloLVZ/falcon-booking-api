package com.falcon.booking.domain.mapper;

import com.falcon.booking.persistence.entity.ReservationEntity;
import com.falcon.booking.web.dto.reservation.ResponseReservationDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;

@Component
public class ReservationMapper {

    private final FlightMapper flightMapper;
    private final PassengerReservationMapper passengerReservationMapper;

    @Autowired
    public ReservationMapper(FlightMapper flightMapper, PassengerReservationMapper passengerReservationMapper) {
        this.flightMapper = flightMapper;
        this.passengerReservationMapper = passengerReservationMapper;
    }

    public ResponseReservationDto toResponseDto(ReservationEntity entity) {

        return new ResponseReservationDto(
                entity.getNumber(),
                entity.getContactEmail(),
                entity.getDatetimeReservation(),
                entity.getStatus(),
                flightMapper.toDto(entity.getFlight()),
                passengerReservationMapper.toResponseDto(entity.getPassengerReservations())
        );
    }

    public List<ResponseReservationDto> toResponseDto(List<ReservationEntity> entities) {
        List<ResponseReservationDto> dtos = new ArrayList<>();
        for (ReservationEntity entity : entities) {
            dtos.add(toResponseDto(entity));
        }
        return dtos;
    }
}