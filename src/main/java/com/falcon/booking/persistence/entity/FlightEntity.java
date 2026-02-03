package com.falcon.booking.persistence.entity;

import com.falcon.booking.domain.exception.Flight.FlightInvalidStatusChangeException;
import com.falcon.booking.domain.valueobject.FlightStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.List;
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

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "flight", orphanRemoval = true, cascade = CascadeType.ALL)
    public List<PassengerReservationEntity> reservations;

    public boolean isScheduled() {
        return this.status.equals(FlightStatus.SCHEDULED);
    }

    public boolean isCheckInAvailable() {
        return this.status.equals(FlightStatus.CHECK_IN_AVAILABLE);
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

    public boolean canBeReserved() {
        return (this.isScheduled() || this.isCheckInAvailable());
    }

    public void cancel(){
        if (this.isCanceled() ) return;

        if(this.isInBoarding() || this.isCompleted())
            throw new FlightInvalidStatusChangeException(this.status, FlightStatus.CANCELED);

        this.status = FlightStatus.CANCELED;
    }

    public void startCheckIn(){
        if (this.isCheckInAvailable() ) return;

        if(!this.isScheduled())
            throw new FlightInvalidStatusChangeException(this.status, FlightStatus.CHECK_IN_AVAILABLE);

        this.status = FlightStatus.CHECK_IN_AVAILABLE;
    }

    public void startBoarding(){
        if (this.isInBoarding() ) return;

        if(!this.isCheckInAvailable())
            throw new FlightInvalidStatusChangeException(this.status, FlightStatus.BOARDING);

        this.status = FlightStatus.BOARDING;
    }

    public void markAsCompleted(){
        if (this.isCompleted() ) return;

        if(!this.isInBoarding())
            throw new FlightInvalidStatusChangeException(this.status, FlightStatus.COMPLETED);

        this.status = FlightStatus.COMPLETED;
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
