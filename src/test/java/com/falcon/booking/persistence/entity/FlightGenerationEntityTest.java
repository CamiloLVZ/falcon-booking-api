package com.falcon.booking.persistence.entity;


import com.falcon.booking.domain.valueobject.FlightGenerationStatus;
import com.falcon.booking.domain.valueobject.FlightGenerationType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

public class FlightGenerationEntityTest {


    @DisplayName("Should mark as complete and update entity fields")
    @Test
    void shouldMarkAsCompleteAndUpdate(){
        FlightGenerationEntity entity = FlightGenerationEntity.startGlobalGeneration();
        Instant before = Instant.now();

        entity.markAsCompleted(1000);
        Instant after = Instant.now();

        assertThat(entity.getStatus()).isEqualTo(FlightGenerationStatus.COMPLETED);
        assertThat(entity.getFinishedAt()).isBetween(before, after);
        assertThat(entity.getTotalGenerated()).isEqualTo(1000);
    }


    @DisplayName("Should mark as failed and update entity fields")
    @Test
    void shouldMarkAsFailedAndUpdate(){
        FlightGenerationEntity entity = FlightGenerationEntity.startGlobalGeneration();
        Instant before = Instant.now();

        entity.markAsFailed();
        Instant after = Instant.now();

        assertThat(entity.getStatus()).isEqualTo(FlightGenerationStatus.FAILED);
        assertThat(entity.getFinishedAt()).isBetween(before, after);
    }

    @DisplayName("Should return a new global flights generation entity with correct initial values")
    @Test
    void shouldReturnNewGlobalGeneration(){
        Instant before = Instant.now();

        FlightGenerationEntity entity = FlightGenerationEntity.startGlobalGeneration();
        Instant after = Instant.now();

        assertThat(entity.getIdRoute()).isNull();
        assertThat(entity.getTargetDate()).isNull();
        assertThat(entity.getType()).isEqualTo(FlightGenerationType.GLOBAL);
        assertThat(entity.getStatus()).isEqualTo(FlightGenerationStatus.RUNNING);
        assertThat(entity.getStartedAt()).isBetween(before, after);
        assertThat(entity.getFinishedAt()).isNull();
        assertThat(entity.getTotalGenerated()).isNull();
    }

    @DisplayName("Should return a new route flights generation entity with correct initial values")
    @Test
    void shouldReturnNewRouteGeneration(){
        Instant before = Instant.now();

        FlightGenerationEntity entity = FlightGenerationEntity.startRouteGeneration(1L);
        Instant after = Instant.now();

        assertThat(entity.getIdRoute()).isEqualTo(1L);
        assertThat(entity.getTargetDate()).isNull();
        assertThat(entity.getType()).isEqualTo(FlightGenerationType.ROUTE);
        assertThat(entity.getStatus()).isEqualTo(FlightGenerationStatus.RUNNING);
        assertThat(entity.getStartedAt()).isBetween(before, after);
        assertThat(entity.getFinishedAt()).isNull();
        assertThat(entity.getTotalGenerated()).isNull();
    }


    @DisplayName("Should return a new daily flights generation entity with correct initial values")
    @Test
    void shouldReturnNewDailyGeneration(){
        Instant before = Instant.now();
        LocalDate date = LocalDate.now();

        FlightGenerationEntity entity = FlightGenerationEntity.startDailyGeneration(date);
        Instant after = Instant.now();

        assertThat(entity.getIdRoute()).isNull();
        assertThat(entity.getTargetDate()).isEqualTo(date);
        assertThat(entity.getType()).isEqualTo(FlightGenerationType.DAILY);
        assertThat(entity.getStatus()).isEqualTo(FlightGenerationStatus.RUNNING);
        assertThat(entity.getStartedAt()).isBetween(before, after);
        assertThat(entity.getFinishedAt()).isNull();
        assertThat(entity.getTotalGenerated()).isNull();
    }

}
