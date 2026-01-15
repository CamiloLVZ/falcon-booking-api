package com.falcon.booking.persistence.entity;

import com.falcon.booking.domain.valueobject.RouteStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;

@Entity
@Table(name = "route")
@NoArgsConstructor
@Getter
@Setter
public class RouteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_airport_origin",nullable = false)
    AirportEntity airportOrigin;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_airport_destination",nullable = false)
    AirportEntity airportDestination;

    @Column(name = "flight_number", nullable = false, unique = true)
    String flightNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_default_airplane_type", nullable = false)
    AirplaneTypeEntity defaultAirplaneType;

    @Column(name = "length_minutes", nullable = false)
    Integer lengthMinutes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false,length = 20)
    RouteStatus status;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        if (!(o instanceof RouteEntity other)) return false;

        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

}
