package com.falcon.booking.persistence.entity;

import com.falcon.booking.domain.valueobject.FlightGenerationStatus;
import com.falcon.booking.domain.valueobject.FlightGenerationType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "flight_generation")
@Getter
@Setter
public class FlightGenerationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "id_route")
    private Long idRoute;

    @Column(name="target_date")
    private LocalDate targetDate;

    @Column(name="type", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private FlightGenerationType type;

    @Column(name="status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private FlightGenerationStatus status;

    @Column(name = "started_at", nullable = false)
    private Instant startedAt;

    @Column(name = "finished_at")
    private Instant finishedAt;

    @Column(name = "total_generated")
    private Integer totalGenerated;

    public void markAsCompleted(int totalGenerated) {
        this.status = FlightGenerationStatus.COMPLETED;
        this.finishedAt = Instant.now();
        this.totalGenerated = totalGenerated;
    }

    public void markAsFailed() {
        this.status = FlightGenerationStatus.FAILED;
        this.finishedAt = Instant.now();
    }

    protected FlightGenerationEntity() {}

    public static FlightGenerationEntity startGlobalGeneration() {
        FlightGenerationEntity entity = new FlightGenerationEntity();
        entity.setType(FlightGenerationType.GLOBAL);
        entity.setStatus(FlightGenerationStatus.RUNNING);
        entity.setStartedAt(Instant.now());
        return entity;
    }

    public static FlightGenerationEntity startRouteGeneration(Long idRoute) {
        FlightGenerationEntity entity = new FlightGenerationEntity();
        entity.setType(FlightGenerationType.ROUTE);
        entity.setIdRoute(idRoute);
        entity.setStatus(FlightGenerationStatus.RUNNING);
        entity.setStartedAt(Instant.now());
        return entity;
    }

    public static FlightGenerationEntity startDailyGeneration(LocalDate targetDate) {
        FlightGenerationEntity entity = new FlightGenerationEntity();
        entity.setType(FlightGenerationType.DAILY);
        entity.setTargetDate(targetDate);
        entity.setStatus(FlightGenerationStatus.RUNNING);
        entity.setStartedAt(Instant.now());
        return entity;
    }
}
