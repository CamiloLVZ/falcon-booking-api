package com.falcon.booking.feature.flightGeneration.mapper;

import com.falcon.booking.common.enums.FlightGenerationStatus;
import com.falcon.booking.common.enums.FlightGenerationType;
import com.falcon.booking.feature.flightGeneration.dto.ResponseFlightsGenerationDto;
import com.falcon.booking.persistence.entity.FlightGenerationEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class FlightGenerationMapperTest {

    @InjectMocks
    private FlightGenerationMapper flightGenerationMapper;

    @DisplayName("Should map entity to dto with duration when both timestamps present")
    @Test
    void shouldMapToDtoWithDuration() {
        Instant startedAt = Instant.now();
        Instant finishedAt = startedAt.plusSeconds(30);

        FlightGenerationEntity entity = FlightGenerationEntity.startGlobalGeneration();
        entity.setId(1L);
        entity.setStatus(FlightGenerationStatus.COMPLETED);
        entity.setIdRoute(10L);
        entity.setTotalGenerated(100);
        entity.setStartedAt(startedAt);
        entity.setFinishedAt(finishedAt);

        ResponseFlightsGenerationDto result = flightGenerationMapper.toDto(entity);

        assertThat(result.generationId()).isEqualTo(1L);
        assertThat(result.status()).isEqualTo(FlightGenerationStatus.COMPLETED);
        assertThat(result.type()).isEqualTo(FlightGenerationType.GLOBAL);
        assertThat(result.routeId()).isEqualTo(10L);
        assertThat(result.totalGenerated()).isEqualTo(100);
        assertThat(result.startedAt()).isEqualTo(startedAt);
        assertThat(result.finishedAt()).isEqualTo(finishedAt);
        assertThat(result.durationSeconds()).isEqualTo(30L);
        assertThat(result.statusUrl()).isEqualTo("api/v1/flights/generations/1");
    }

    @DisplayName("Should map entity to dto with null duration when finishedAt is null")
    @Test
    void shouldMapToDtoWithNullDuration_whenFinishedAtIsNull() {
        Instant startedAt = Instant.now();

        FlightGenerationEntity entity = FlightGenerationEntity.startRouteGeneration(10L);
        entity.setId(2L);
        entity.setStatus(FlightGenerationStatus.RUNNING);
        entity.setStartedAt(startedAt);
        entity.setFinishedAt(null);

        ResponseFlightsGenerationDto result = flightGenerationMapper.toDto(entity);

        assertThat(result.generationId()).isEqualTo(2L);
        assertThat(result.status()).isEqualTo(FlightGenerationStatus.RUNNING);
        assertThat(result.type()).isEqualTo(FlightGenerationType.ROUTE);
        assertThat(result.routeId()).isEqualTo(10L);
        assertThat(result.totalGenerated()).isNull();
        assertThat(result.finishedAt()).isNull();
        assertThat(result.durationSeconds()).isNull();
    }

    @DisplayName("Should map entity to dto with null duration when startedAt is null")
    @Test
    void shouldMapToDtoWithNullDuration_whenStartedAtIsNull() {
        FlightGenerationEntity entity = FlightGenerationEntity.startGlobalGeneration();
        entity.setId(3L);
        entity.setStatus(FlightGenerationStatus.RUNNING);
        entity.setStartedAt(null);
        entity.setFinishedAt(Instant.now());

        ResponseFlightsGenerationDto result = flightGenerationMapper.toDto(entity);

        assertThat(result.durationSeconds()).isNull();
    }

    @DisplayName("Should map entity list to dto list")
    @Test
    void shouldMapListToDto() {
        Instant now = Instant.now();

        FlightGenerationEntity first = FlightGenerationEntity.startRouteGeneration(10L);
        first.setId(1L);
        first.setStatus(FlightGenerationStatus.COMPLETED);
        first.setTotalGenerated(50);
        first.setStartedAt(now);
        first.setFinishedAt(now.plusSeconds(10));

        FlightGenerationEntity second = FlightGenerationEntity.startGlobalGeneration();
        second.setId(2L);
        second.setStatus(FlightGenerationStatus.RUNNING);
        second.setStartedAt(now);
        second.setFinishedAt(null);

        List<ResponseFlightsGenerationDto> result = flightGenerationMapper.toDto(List.of(first, second));

        assertThat(result).hasSize(2);
        assertThat(result.get(0).generationId()).isEqualTo(1L);
        assertThat(result.get(0).durationSeconds()).isEqualTo(10L);
        assertThat(result.get(1).generationId()).isEqualTo(2L);
        assertThat(result.get(1).durationSeconds()).isNull();
    }
}
