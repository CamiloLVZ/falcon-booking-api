package com.falcon.booking.feature.reservation.mapper;

import com.falcon.booking.common.enums.PassengerReservationStatus;
import com.falcon.booking.feature.passenger.dto.ResponsePassengerDto;
import com.falcon.booking.feature.passenger.mapper.PassengerMapper;
import com.falcon.booking.feature.reservation.dto.ResponsePassengerReservationDto;
import com.falcon.booking.persistence.entity.AirplaneTypeEntity;
import com.falcon.booking.persistence.entity.FlightEntity;
import com.falcon.booking.persistence.entity.PassengerEntity;
import com.falcon.booking.persistence.entity.PassengerReservationEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class PassengerReservationMapperTest {

    @Mock
    private PassengerMapper passengerMapper;

    @InjectMocks
    private PassengerReservationMapper passengerReservationMapper;

    @DisplayName("Should map passenger reservation entity to response dto")
    @Test
    void shouldMapPassengerReservationEntityToResponseDto() {
        PassengerEntity passenger = new PassengerEntity();
        ResponsePassengerDto passengerDto = new ResponsePassengerDto(1L, "ANA", "DIAZ", null, "CO",
                LocalDate.of(1999, 3, 12), "PP001", "123");

        AirplaneTypeEntity airplaneType = mock(AirplaneTypeEntity.class);
        given(airplaneType.getSeatLabel(14)).willReturn("3B");

        FlightEntity flight = mock(FlightEntity.class);
        given(flight.getAirplaneType()).willReturn(airplaneType);

        PassengerReservationEntity entity = mock(PassengerReservationEntity.class);
        given(entity.getFlight()).willReturn(flight);
        given(entity.getPassenger()).willReturn(passenger);
        given(entity.getSeatNumber()).willReturn(14);
        given(entity.getStatus()).willReturn(PassengerReservationStatus.CHECKED_IN);
        given(passengerMapper.toResponseDto(passenger)).willReturn(passengerDto);

        ResponsePassengerReservationDto result = passengerReservationMapper.toResponseDto(entity);

        assertThat(result.passenger()).isEqualTo(passengerDto);
        assertThat(result.seatNumber()).isEqualTo(14);
        assertThat(result.seatLabel()).isEqualTo("3B");
        assertThat(result.status()).isEqualTo(PassengerReservationStatus.CHECKED_IN);
    }

    @DisplayName("Should map passenger reservation entity list to dto list")
    @Test
    void shouldMapPassengerReservationEntityListToDtoList() {
        PassengerEntity passenger = new PassengerEntity();
        ResponsePassengerDto passengerDto = new ResponsePassengerDto(2L, "LUIS", "RAMOS", null, "PE",
                LocalDate.of(2000, 8, 20), "PP002", "456");

        AirplaneTypeEntity airplaneType = mock(AirplaneTypeEntity.class);
        given(airplaneType.getSeatLabel(1)).willReturn("1A");
        given(airplaneType.getSeatLabel(2)).willReturn("1B");

        FlightEntity flight = mock(FlightEntity.class);
        given(flight.getAirplaneType()).willReturn(airplaneType);

        PassengerReservationEntity first = mock(PassengerReservationEntity.class);
        given(first.getFlight()).willReturn(flight);
        given(first.getPassenger()).willReturn(passenger);
        given(first.getSeatNumber()).willReturn(1);
        given(first.getStatus()).willReturn(PassengerReservationStatus.RESERVED);

        PassengerReservationEntity second = mock(PassengerReservationEntity.class);
        given(second.getFlight()).willReturn(flight);
        given(second.getPassenger()).willReturn(passenger);
        given(second.getSeatNumber()).willReturn(2);
        given(second.getStatus()).willReturn(PassengerReservationStatus.CANCELED);

        given(passengerMapper.toResponseDto(passenger)).willReturn(passengerDto);

        List<ResponsePassengerReservationDto> result = passengerReservationMapper.toResponseDto(List.of(first, second));

        assertThat(result).hasSize(2);
        assertThat(result.get(0).seatNumber()).isEqualTo(1);
        assertThat(result.get(1).status()).isEqualTo(PassengerReservationStatus.CANCELED);
    }
}
