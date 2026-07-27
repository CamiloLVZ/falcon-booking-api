package com.falcon.booking.feature.airplaneType.mapper;

import com.falcon.booking.common.enums.AirplaneTypeStatus;
import com.falcon.booking.feature.airplaneType.dto.AirplaneTypeInFlightDto;
import com.falcon.booking.feature.airplaneType.dto.CreateAirplaneTypeDto;
import com.falcon.booking.feature.airplaneType.dto.ResponseAirplaneTypeDto;
import com.falcon.booking.feature.airplaneType.exception.InvalidSeatConfigurationException;
import com.falcon.booking.persistence.entity.AirplaneTypeEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class AirplaneTypeMapperTest {

    @InjectMocks
    private AirplaneTypeMapperImpl airplaneTypeMapper;

    private AirplaneTypeEntity createEntity() {
        AirplaneTypeEntity entity = new AirplaneTypeEntity();
        entity.setId(1L);
        entity.setProducer("AIRBUS");
        entity.setModel("A320");
        entity.setEconomySeats(150);
        entity.setFirstClassSeats(20);
        entity.setSeatColumns("ABCDEF");
        entity.setStatus(AirplaneTypeStatus.ACTIVE);
        return entity;
    }

    @DisplayName("Should map entity to ResponseAirplaneTypeDto")
    @Test
    void shouldMapToResponseDto() {
        AirplaneTypeEntity entity = createEntity();

        ResponseAirplaneTypeDto result = airplaneTypeMapper.toResponseDto(entity);

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.producer()).isEqualTo("AIRBUS");
        assertThat(result.model()).isEqualTo("A320");
        assertThat(result.economySeats()).isEqualTo(150);
        assertThat(result.firstClassSeats()).isEqualTo(20);
        assertThat(result.seatColumns()).isEqualTo("ABCDEF");
        assertThat(result.status()).isEqualTo(AirplaneTypeStatus.ACTIVE);
    }

    @DisplayName("Should map entity list to ResponseAirplaneTypeDto list")
    @Test
    void shouldMapListToResponseDto() {
        AirplaneTypeEntity first = createEntity();
        AirplaneTypeEntity second = new AirplaneTypeEntity();
        second.setId(2L);
        second.setProducer("BOEING");
        second.setModel("737");
        second.setEconomySeats(160);
        second.setFirstClassSeats(16);
        second.setSeatColumns("ABCDEF");
        second.setStatus(AirplaneTypeStatus.INACTIVE);

        List<ResponseAirplaneTypeDto> result = airplaneTypeMapper.toResponseDto(List.of(first, second));

        assertThat(result).hasSize(2);
        assertThat(result.get(0).producer()).isEqualTo("AIRBUS");
        assertThat(result.get(1).producer()).isEqualTo("BOEING");
    }

    @DisplayName("Should map entity to AirplaneTypeInFlightDto")
    @Test
    void shouldMapToInFlightDto() {
        AirplaneTypeEntity entity = createEntity();

        AirplaneTypeInFlightDto result = airplaneTypeMapper.toInFlightDto(entity);

        assertThat(result.producer()).isEqualTo("AIRBUS");
        assertThat(result.model()).isEqualTo("A320");
        assertThat(result.economySeats()).isEqualTo(150);
        assertThat(result.firstClassSeats()).isEqualTo(20);
        assertThat(result.seatColumns()).isEqualTo("ABCDEF");
    }

    @DisplayName("Should map CreateAirplaneTypeDto to entity ignoring seat fields")
    @Test
    void shouldMapCreateDtoToEntity() {
        CreateAirplaneTypeDto dto = new CreateAirplaneTypeDto("BOEING", "737", 150, 20, "ABCDEF");

        AirplaneTypeEntity result = airplaneTypeMapper.toEntity(dto);

        assertThat(result.getProducer()).isEqualTo("BOEING");
        assertThat(result.getModel()).isEqualTo("737");
        assertThat(result.getEconomySeats()).isNull();
        assertThat(result.getFirstClassSeats()).isNull();
        assertThat(result.getSeatColumns()).isNull();
        assertThat(result.getId()).isNull();
        assertThat(result.getStatus()).isNull();
    }
}
