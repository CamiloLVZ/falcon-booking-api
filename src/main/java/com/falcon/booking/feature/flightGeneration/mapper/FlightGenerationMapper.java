package com.falcon.booking.feature.flightGeneration.mapper;

import com.falcon.booking.common.enums.FlightGenerationStatus;
import com.falcon.booking.common.enums.FlightGenerationType;
import com.falcon.booking.feature.flightGeneration.dto.ResponseFlightsGenerationDto;
import com.falcon.booking.persistence.entity.FlightGenerationEntity;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Component
public class FlightGenerationMapper {

    public List<ResponseFlightsGenerationDto> toDto(List<FlightGenerationEntity> entities){
        return entities.stream().
                map(this::toDto).
                toList();
    }

    public ResponseFlightsGenerationDto toDto(FlightGenerationEntity entity) {
        Long generationId = entity.getId();
        FlightGenerationStatus status = entity.getStatus();
        FlightGenerationType type = entity.getType();
        Long routeId = entity.getIdRoute();
        Integer totalGenerated = entity.getTotalGenerated();
        Instant startedAt = entity.getStartedAt();
        Instant finishedAt = entity.getFinishedAt();
        String statusUrl = "api/v1/flights/generations/" + entity.getId();

        Long durationSeconds = null;
        if (entity.getStartedAt() != null && entity.getFinishedAt() != null) {
            durationSeconds = Duration.between(entity.getStartedAt(), entity.getFinishedAt()).toSeconds();
        }

        return new ResponseFlightsGenerationDto(generationId, status, type, routeId, totalGenerated, startedAt,
                                                finishedAt, durationSeconds, statusUrl);
    }

}
