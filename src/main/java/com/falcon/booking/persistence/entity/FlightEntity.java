package com.falcon.booking.persistence.entity;

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

    public FlightEntity(RouteEntity route, AirplaneTypeEntity airplaneType, OffsetDateTime departureDateTime, FlightStatus status) {
        this.route = route;
        this.airplaneType = airplaneType;
        this.departureDateTime = departureDateTime;
        this.status = status;
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
