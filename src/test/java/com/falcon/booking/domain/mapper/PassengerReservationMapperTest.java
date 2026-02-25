package com.falcon.booking.domain.mapper;

import com.falcon.booking.domain.valueobject.PassengerReservationStatus;
import com.falcon.booking.persistence.entity.PassengerEntity;
import com.falcon.booking.persistence.entity.PassengerReservationEntity;
import com.falcon.booking.web.dto.passenger.ResponsePassengerDto;
import com.falcon.booking.web.dto.reservation.ResponsePassengerReservationDto;
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

        PassengerReservationEntity entity = mock(PassengerReservationEntity.class);
        given(entity.getPassenger()).willReturn(passenger);
        given(entity.getSeatNumber()).willReturn(14);
        given(entity.getStatus()).willReturn(PassengerReservationStatus.CHECKED_IN);
        given(passengerMapper.toResponseDto(passenger)).willReturn(passengerDto);

        ResponsePassengerReservationDto result = passengerReservationMapper.toResponseDto(entity);

        assertThat(result.passenger()).isEqualTo(passengerDto);
        assertThat(result.seatNumber()).isEqualTo(14);
        assertThat(result.status()).isEqualTo(PassengerReservationStatus.CHECKED_IN);
    }

    @DisplayName("Should map passenger reservation entity list to dto list")
    @Test
    void shouldMapPassengerReservationEntityListToDtoList() {
        PassengerEntity passenger = new PassengerEntity();
        ResponsePassengerDto passengerDto = new ResponsePassengerDto(2L, "LUIS", "RAMOS", null, "PE",
                LocalDate.of(2000, 8, 20), "PP002", "456");

        PassengerReservationEntity first = mock(PassengerReservationEntity.class);
        given(first.getPassenger()).willReturn(passenger);
        given(first.getSeatNumber()).willReturn(1);
        given(first.getStatus()).willReturn(PassengerReservationStatus.RESERVED);

        PassengerReservationEntity second = mock(PassengerReservationEntity.class);
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
