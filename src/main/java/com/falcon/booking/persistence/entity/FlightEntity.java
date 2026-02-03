package com.falcon.booking.persistence.entity;

import com.falcon.booking.domain.exception.Flight.FlightInvalidStatusChangeException;
import com.falcon.booking.domain.valueobject.FlightStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.Objects;

@Entity
@Table(name = "flight", uniqueConstraints = {
        @UniqueConstraint(name = "uk_id_route_departure_datetime", columnNames = {"id_route", "departure_datetime"})
})
@NoArgsConstructor
@Getter
@Setter
public class FlightEntity {

    public FlightEntity(RouteEntity route, AirplaneTypeEntity airplaneType, OffsetDateTime departureDateTime, FlightStatus status) {
        this.route = route;
        this.airplaneType = airplaneType;
        this.departureDateTime = departureDateTime;
        this.status = status;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_route", nullable = false)
    RouteEntity route;

    @Column(name = "departure_datetime", nullable = false)
    OffsetDateTime departureDateTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_airplane_type", nullable = false)
    AirplaneTypeEntity airplaneType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    FlightStatus status;

    public boolean isScheduled() {
        return this.status.equals(FlightStatus.SCHEDULED);
    }

    public boolean isInBoarding() {
        return this.status.equals(FlightStatus.BOARDING);
    }

    public boolean isCompleted() {
        return this.status.equals(FlightStatus.COMPLETED);
    }

    public boolean isCanceled() {
        return this.status.equals(FlightStatus.CANCELED);
    }

    public void cancel(){
        if(this.status.equals(FlightStatus.BOARDING))
            throw new FlightInvalidStatusChangeException(FlightStatus.BOARDING, FlightStatus.CANCELED);

        if(this.status.equals(FlightStatus.COMPLETED))
            throw new FlightInvalidStatusChangeException(FlightStatus.COMPLETED, FlightStatus.CANCELED);

        this.status = FlightStatus.CANCELED;
    }

    public void startBoarding(){
        if(this.status.equals(FlightStatus.COMPLETED))
            throw new FlightInvalidStatusChangeException(FlightStatus.COMPLETED, FlightStatus.BOARDING);

        if(this.status.equals(FlightStatus.CANCELED))
            throw new FlightInvalidStatusChangeException(FlightStatus.CANCELED, FlightStatus.BOARDING);

        this.status = FlightStatus.BOARDING;
    }

    public void markAsComplete(){
        if(this.status.equals(FlightStatus.CANCELED))
            throw new FlightInvalidStatusChangeException(FlightStatus.CANCELED, FlightStatus.COMPLETED);
        if(this.status.equals(FlightStatus.SCHEDULED))
            throw new FlightInvalidStatusChangeException(FlightStatus.SCHEDULED, FlightStatus.COMPLETED);
        this.status = FlightStatus.COMPLETED;
    }

    public boolean canBeReserved() {
        return this.status.equals(FlightStatus.SCHEDULED);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FlightEntity that = (FlightEntity) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }


}
