package com.falcon.booking.domain.mapper;

import com.falcon.booking.domain.valueobject.FlightGenerationStatus;
import com.falcon.booking.domain.valueobject.FlightGenerationType;
import com.falcon.booking.persistence.entity.FlightGenerationEntity;
import com.falcon.booking.web.dto.flight.ResponseFlightsGenerationDto;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Component
public class FlightGenerationMapper {

    public List<ResponseFlightsGenerationDto> toDto(List<FlightGenerationEntity> entities){
        List<ResponseFlightsGenerationDto> dtos = new ArrayList<>();
        for (FlightGenerationEntity entity : entities) {
            dtos.add(toDto(entity));
        }
        return dtos;
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
