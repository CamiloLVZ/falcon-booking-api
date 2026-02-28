package com.falcon.booking.domain.mapper;

import com.falcon.booking.domain.valueobject.ReservationStatus;
import com.falcon.booking.persistence.entity.FlightEntity;
import com.falcon.booking.persistence.entity.PassengerReservationEntity;
import com.falcon.booking.persistence.entity.ReservationEntity;
import com.falcon.booking.web.dto.flight.ResponseFlightDto;
import com.falcon.booking.web.dto.reservation.ResponsePassengerReservationDto;
import com.falcon.booking.web.dto.reservation.ResponseReservationDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class ReservationMapperTest {

    @Mock
    private FlightMapper flightMapper;

    @Mock
    private PassengerReservationMapper passengerReservationMapper;

    @InjectMocks
    private ReservationMapper reservationMapper;

    @DisplayName("Should map reservation entity to response dto")
    @Test
    void shouldMapReservationEntityToResponseDto() {
        FlightEntity flightEntity = mock(FlightEntity.class);
        PassengerReservationEntity passengerReservationEntity = mock(PassengerReservationEntity.class);
        ReservationEntity entity = mock(ReservationEntity.class);

        Instant reservationDateTime = Instant.parse("2025-01-10T10:15:30Z");
        ResponseFlightDto flightDto = mock(ResponseFlightDto.class);
        ResponsePassengerReservationDto passengerDto = mock(ResponsePassengerReservationDto.class);

        given(entity.getNumber()).willReturn("RES123");
        given(entity.getContactEmail()).willReturn("test@mail.com");
        given(entity.getDatetimeReservation()).willReturn(reservationDateTime);
        given(entity.getStatus()).willReturn(ReservationStatus.RESERVED);
        given(entity.getFlight()).willReturn(flightEntity);
        given(entity.getPassengerReservations()).willReturn(List.of(passengerReservationEntity));
        given(flightMapper.toDto(flightEntity)).willReturn(flightDto);
        given(passengerReservationMapper.toResponseDto(List.of(passengerReservationEntity))).willReturn(List.of(passengerDto));

        ResponseReservationDto result = reservationMapper.toResponseDto(entity);

        assertThat(result.number()).isEqualTo("RES123");
        assertThat(result.contactEmail()).isEqualTo("test@mail.com");
        assertThat(result.datetimeReservation()).isEqualTo(reservationDateTime);
        assertThat(result.status()).isEqualTo(ReservationStatus.RESERVED);
        assertThat(result.flight()).isEqualTo(flightDto);
        assertThat(result.passengers()).containsExactly(passengerDto);
    }

    @DisplayName("Should map reservation entity list to response dto list")
    @Test
    void shouldMapReservationEntityListToResponseDtoList() {
        ReservationEntity first = mock(ReservationEntity.class);
        ReservationEntity second = mock(ReservationEntity.class);

        ResponseReservationDto firstDto = mock(ResponseReservationDto.class);
        ResponseReservationDto secondDto = mock(ResponseReservationDto.class);

        given(first.getNumber()).willReturn("RES1");
        given(second.getNumber()).willReturn("RES2");

        given(first.getContactEmail()).willReturn("a@mail.com");
        given(second.getContactEmail()).willReturn("b@mail.com");

        given(first.getDatetimeReservation()).willReturn(Instant.parse("2025-01-01T00:00:00Z"));
        given(second.getDatetimeReservation()).willReturn(Instant.parse("2025-01-02T00:00:00Z"));

        given(first.getStatus()).willReturn(ReservationStatus.RESERVED);
        given(second.getStatus()).willReturn(ReservationStatus.CANCELED);

        FlightEntity flight = mock(FlightEntity.class);
        given(first.getFlight()).willReturn(flight);
        given(second.getFlight()).willReturn(flight);

        List<PassengerReservationEntity> passengers = List.of();
        given(first.getPassengerReservations()).willReturn(passengers);
        given(second.getPassengerReservations()).willReturn(passengers);

        ResponseFlightDto flightDto = mock(ResponseFlightDto.class);
        given(flightMapper.toDto(flight)).willReturn(flightDto);
        given(passengerReservationMapper.toResponseDto(passengers)).willReturn(List.of());

        // We can reuse the real mapper logic for list conversion
        List<ResponseReservationDto> result = reservationMapper.toResponseDto(List.of(first, second));

        assertThat(result).hasSize(2);
        assertThat(result.get(0).number()).isEqualTo("RES1");
        assertThat(result.get(1).number()).isEqualTo("RES2");
    }
}
